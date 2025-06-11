package xyz.waranim.common.kafka;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaJsonConfig {

    @Bean
    ProducerFactory<String, OrderStatusEvent> producerFactory(KafkaProperties properties) {
        Map<String, Object> cfg = new HashMap<>(properties.buildProducerProperties());
        cfg.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(cfg);
    }

    @Bean
    KafkaTemplate<String, OrderStatusEvent> kafkaTemplate(
            ProducerFactory<String, OrderStatusEvent> pf) {
        return new KafkaTemplate<>(pf);
    }
}
