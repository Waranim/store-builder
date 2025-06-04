package xyz.waranim.notificationservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import xyz.waranim.common.kafka.OrderStatusEvent;

import java.util.Map;

@Configuration
@EnableKafka
public class EmailConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderStatusEvent> consumerFactory() {
        Map<String,Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092",
                ConsumerConfig.GROUP_ID_CONFIG, "email",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        );
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderStatusEvent> factory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, OrderStatusEvent>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        return factory;
    }
}
