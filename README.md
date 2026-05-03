# app-mvn-d5-scoring-api

> Back de **D5 Vendor Scoring** del ecosistema W2M Virtual.
> Spring Boot 3.5.5 + Java 24 + Maven 3.9 multi-módulo. Kafka KRaft (single broker).
> Puerto del API: **8087**.

## Qué hace

Consume eventos `BookingEvent` (`CONFIRMED` | `REJECTED`) del topic `bookings.events`
publicados por `app-mvn-d3-supplier-api` y mantiene un score agregado por supplier
en memoria. Expone API REST de consulta.

Primer flujo asíncrono del sistema (event-driven choreography).

## Módulos

```
app-mvn-d5-scoring-api/
├── pom.xml                          (parent — packaging pom)
├── docker-compose.yml               (Kafka KRaft :9092)
├── scoring/                         (dominio + adapters)
└── app/                             (arranque + CORS)
```

## Cómo arrancar

```bash
docker compose up -d                # Kafka KRaft
mvn -DskipTests package
java -jar app/target/app-0.1.0-SNAPSHOT.jar
```

## Endpoints

| Método | Path | Descripción |
|---|---|---|
| `GET` | `/api/scoring/suppliers` | Lista scores por supplier |
| `GET` | `/api/scoring/suppliers/{supplierId}` | Score de un supplier |
| `GET` | `/actuator/health` | Health probe |

## Fórmula del score

```
score = round( (confirmed - rejected * 3.0) / max(1, totalAttempts) * 100 )
```

Saturado a `[0..100]`. Penalización 3:1 a rechazos.

## Eventos consumidos

Topic `bookings.events`:

```json
{
  "bookingId": "uuid",
  "supplierId": "uuid",
  "hotelId": "string",
  "status": "CONFIRMED" | "REJECTED",
  "occurredAt": "2026-05-03T12:34:56Z"
}
```
