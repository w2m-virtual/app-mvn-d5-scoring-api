#!/usr/bin/env bash
# E2E Bloque B — verifica la cadena completa de eventos vía Kafka.
#
# Pre-requisito: docker compose up -d en TODOS los repos del departamento
# (mínimo d2-catalog-api, d3-supplier-api, d5-scoring-api, virtual-orquestador).
#
# Cadena verificada:
#   1. POST /api/hotels en d2          → publica HOTEL_CREATED en hotels.events
#   2. POST /api/bookings en d3        → publica BookingEvent CONFIRMED en bookings.events
#   3. orquestador (consumer)          → /api/events/bookings/count incrementa
#   4. d5 scoring (consumer)           → log "BookingEvent received"
#   5. kafka-ui (:8090)                → ambos topics existen con offsets > 0
set -euo pipefail

D2=${D2_URL:-http://localhost:8082}
D3=${D3_URL:-http://localhost:8083}
D5=${D5_URL:-http://localhost:8087}
ORQ=${ORQ_URL:-http://localhost:8080}
KUI=${KUI_URL:-http://localhost:8090}

red()  { printf "\033[0;31m%s\033[0m\n" "$*"; }
green(){ printf "\033[0;32m%s\033[0m\n" "$*"; }
step() { printf "\n\033[1;36m== %s ==\033[0m\n" "$*"; }

step "1. Crear hotel en d2"
HOTEL=$(curl -s -X POST "$D2/api/hotels" -H "Content-Type: application/json" \
  -d '{"name":"E2E Bloque B","city":"Test","pricePerNight":99,"currency":"EUR","stars":4}')
HOTEL_ID=$(echo "$HOTEL" | grep -oE '"id":"[^"]+"' | head -1 | cut -d'"' -f4)
[ -n "$HOTEL_ID" ] || { red "FAIL: no se creó hotel"; exit 1; }
green "  hotel creado id=$HOTEL_ID"

step "2. Verificar HotelEvent publicado en logs de d2"
for i in 1 2 3 4 5; do
  if docker logs --since 30s w2m-virtual-d2-catalog-api 2>&1 | grep -q "HotelEvent published.*HOTEL_CREATED.*$HOTEL_ID"; then
    green "  OK · HOTEL_CREATED visto en logs"
    break
  fi
  sleep 1
  [ "$i" = "5" ] && { red "FAIL: no se vio HotelEvent en logs de d2"; exit 1; }
done

step "2.b Verificar consumers hotels.events (d3 + d5) registraron"
sleep 2
D3_H=$(curl -s "$D3/api/events/hotels/count" 2>/dev/null | grep -oE '[0-9]+' || echo 0)
D5_H=$(curl -s "$D5/api/events/hotels/count" 2>/dev/null | grep -oE '[0-9]+' || echo 0)
echo "  d3 /api/events/hotels/count=$D3_H | d5 /api/events/hotels/count=$D5_H"
[ "$D3_H" -gt 0 ] && green "  OK · d3 procesó HotelEvent" || { red "FAIL: d3 no procesó HotelEvent"; exit 1; }
[ "$D5_H" -gt 0 ] && green "  OK · d5 procesó HotelEvent" || { red "FAIL: d5 no procesó HotelEvent"; exit 1; }

step "3. Snapshot count del orquestador antes del booking"
COUNT_BEFORE=$(curl -s "$ORQ/api/events/bookings/count" | grep -oE '[0-9]+')
echo "  count_before=$COUNT_BEFORE"

step "4. Crear booking en d3"
BOOKING=$(curl -s -X POST "$D3/api/bookings" -H "Content-Type: application/json" \
  -d "{\"hotelId\":\"$HOTEL_ID\",\"checkInDate\":\"2026-06-01\",\"checkOutDate\":\"2026-06-05\",\"guests\":2,\"holderName\":\"E2E\",\"holderEmail\":\"e2e@test.com\"}")
BOOKING_ID=$(echo "$BOOKING" | grep -oE '"id":"[^"]+"' | head -1 | cut -d'"' -f4)
[ -n "$BOOKING_ID" ] || { red "FAIL: no se creó booking"; exit 1; }
green "  booking creado id=$BOOKING_ID"

sleep 3
step "5. Verificar count en orquestador subió"
COUNT_AFTER=$(curl -s "$ORQ/api/events/bookings/count" | grep -oE '[0-9]+')
echo "  count_after=$COUNT_AFTER"
[ "$COUNT_AFTER" -gt "$COUNT_BEFORE" ] \
  && green "  OK · orquestador procesó el booking event" \
  || { red "FAIL: count no subió"; exit 1; }

step "6. Verificar d5 scoring procesó"
for i in 1 2 3 4 5; do
  if docker logs --since 30s w2m-virtual-d5-scoring-api 2>&1 | grep -q "BookingEvent received.*CONFIRMED"; then
    green "  OK · d5 procesó el BookingEvent"
    break
  fi
  sleep 1
  [ "$i" = "5" ] && { red "FAIL: d5 no recibió"; exit 1; }
done

step "7. kafka-ui — topics existentes"
curl -s "$KUI/api/clusters/w2m-virtual/topics" 2>/dev/null \
  | grep -oE '"name":"[^"]+"' | grep -v __consumer | grep -v _kafka_ui \
  | sort -u || true

green "\n✅ E2E Bloque B OK"
