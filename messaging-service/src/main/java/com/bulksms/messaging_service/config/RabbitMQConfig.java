package com.bulksms.messaging_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SMS_QUEUE = "sms_queue";
    public static final String EXCHANGE = "sms_exchange";
    public static final String ROUTING_KEY = "sms_routing_key";

    public static final String DELIVERY_QUEUE = "delivery_queue";
    public static final String DELIVERY_EXCHANGE = "delivery_exchange";
    public static final String DELIVERY_ROUTING_KEY = "delivery_routing_key";

    @Bean
    public Queue queue() {
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("x-max-priority", 10); // FR-MS5: Enable priority queuing
        return new Queue(SMS_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue deliveryQueue() {
        return new Queue(DELIVERY_QUEUE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange deliveryExchange() {
        return new DirectExchange(DELIVERY_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding deliveryBinding(Queue deliveryQueue, DirectExchange deliveryExchange) {
        return BindingBuilder.bind(deliveryQueue).to(deliveryExchange).with(DELIVERY_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
