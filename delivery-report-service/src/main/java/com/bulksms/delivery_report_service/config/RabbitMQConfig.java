package com.bulksms.delivery_report_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DELIVERY_QUEUE = "delivery_queue";
    public static final String DELIVERY_EXCHANGE = "delivery_exchange";
    public static final String DELIVERY_ROUTING_KEY = "delivery_routing_key";

    @Bean
    public Queue deliveryQueue() {
        return new Queue(DELIVERY_QUEUE, true);
    }

    @Bean
    public DirectExchange deliveryExchange() {
        return new DirectExchange(DELIVERY_EXCHANGE);
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
