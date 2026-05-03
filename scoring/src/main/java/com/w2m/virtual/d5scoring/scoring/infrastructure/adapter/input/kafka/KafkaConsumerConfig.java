package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.kafka;

import com.w2m.virtual.d5scoring.scoring.domain.BookingEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Define dos ConsumerFactories independientes — uno tipado a {@code BookingEvent} para el
 * topic {@code bookings.events}, otro genérico {@code Map<String,Object>} para
 * {@code payments.events} (los pagos no tienen un record Java compartido entre d4 y d5).
 */
@Configuration
public class KafkaConsumerConfig {

    private final String bootstrapServers;

    public KafkaConsumerConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    private Map<String, Object> baseProps(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // No JsonDeserializer.* aquí: el deserializer se pasa pre-construido y configurar
        // ambos a la vez es inválido.
        return props;
    }

    @Bean
    public ConsumerFactory<String, BookingEvent> bookingsConsumerFactory() {
        Map<String, Object> props = baseProps("d5-scoring");
        JsonDeserializer<BookingEvent> deser = new JsonDeserializer<>(BookingEvent.class, false);
        deser.addTrustedPackages("*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deser);
    }

    @Bean(name = "bookingsKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, BookingEvent> bookingsKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BookingEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bookingsConsumerFactory());
        return factory;
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ConsumerFactory<String, Map> paymentsConsumerFactory() {
        Map<String, Object> props = baseProps("d5-scoring-payments");
        JsonDeserializer deser = new JsonDeserializer<>(LinkedHashMap.class, false);
        deser.addTrustedPackages("*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deser);
    }

    @Bean(name = "paymentsKafkaListenerContainerFactory")
    @SuppressWarnings({"rawtypes"})
    public ConcurrentKafkaListenerContainerFactory<String, Map> paymentsKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentsConsumerFactory());
        return factory;
    }
}
