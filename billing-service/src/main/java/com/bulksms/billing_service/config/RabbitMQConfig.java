package com.bulksms.billing_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue billingQueue() {
        return new Queue("billing_queue");
    }

    @Bean
    public TopicExchange billingExchange() {
        return new TopicExchange("billing_exchange");
    }

    @Bean
    public Binding binding(Queue billingQueue, TopicExchange billingExchange) {
        return BindingBuilder.bind(billingQueue).to(billingExchange).with("billing_routing_key");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
