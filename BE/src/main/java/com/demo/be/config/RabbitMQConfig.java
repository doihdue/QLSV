package com.demo.be.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "qlsv.exchange";
    public static final String QUEUE_NAME = "qlsv.notification.queue";
    public static final String ROUTING_KEY_PATTERN = "qlsv.notification.#";
    public static final String ROUTING_KEY_GRADE = "qlsv.notification.grade";

    public static final String REPORT_QUEUE_NAME = "qlsv.report.queue";
    public static final String ROUTING_KEY_REPORT = "qlsv.report.export";

    @Bean
    public TopicExchange qlsvExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange qlsvExchange) {
        return BindingBuilder.bind(notificationQueue).to(qlsvExchange).with(ROUTING_KEY_PATTERN);
    }

    @Bean
    public Queue reportQueue() {
        return new Queue(REPORT_QUEUE_NAME, true);
    }

    @Bean
    public Binding reportBinding(Queue reportQueue, TopicExchange qlsvExchange) {
        return BindingBuilder.bind(reportQueue).to(qlsvExchange).with(ROUTING_KEY_REPORT);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        template.setExchange(EXCHANGE_NAME);
        template.setRoutingKey(ROUTING_KEY_GRADE);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAutoStartup(true);
        return factory;
    }
}
