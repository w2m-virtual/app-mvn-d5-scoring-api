package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.kafka;

import com.w2m.virtual.d5scoring.scoring.application.service.PaymentScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Consume eventos de {@code payments.events} (PAYMENT_CAPTURED / PAYMENT_REFUNDED) y
 * actualiza el agregado global de pagos.
 *
 * <p>Los eventos se reciben como {@code Map<String,Object>} porque d4 y d5 no comparten
 * tipo Java — el discriminador es la propiedad {@code type}.</p>
 */
@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final PaymentScoringService paymentScoringService;

    public PaymentEventConsumer(PaymentScoringService paymentScoringService) {
        this.paymentScoringService = paymentScoringService;
    }

    @KafkaListener(topics = "${scoring.payments-topic:payments.events}",
            groupId = "d5-scoring-payments",
            containerFactory = "paymentsKafkaListenerContainerFactory")
    public void onPaymentEvent(Map<String, Object> event) {
        try {
            if (event == null) {
                return;
            }
            Object type = event.get("type");
            String currency = stringOrNull(event.get("currency"));
            BigDecimal amount = toBigDecimal(event.get("amount"));
            log.info("PaymentEvent received type={} currency={} amount={}", type, currency, amount);

            if ("PAYMENT_CAPTURED".equals(type)) {
                paymentScoringService.applyCaptured(amount, currency);
            } else if ("PAYMENT_REFUNDED".equals(type)) {
                paymentScoringService.applyRefunded(amount, currency);
            } else {
                log.warn("Unknown payment event type={}", type);
            }
        } catch (Exception ex) {
            log.warn("Failed to apply PaymentEvent: {}", ex.getMessage(), ex);
        }
    }

    private static String stringOrNull(Object o) {
        return o == null ? null : o.toString();
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return new BigDecimal(n.toString());
        return new BigDecimal(o.toString());
    }
}
