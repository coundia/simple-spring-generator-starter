package com.pcoundia.helper.amqp.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

public abstract class AmqpSenderHelper<E> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected RabbitTemplate rabbitTemplate;

    protected String exchange = "anpej";
    protected String routingkey;

    public void sendMessage(E item) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> theResult = objectMapper.convertValue(item, Map.class);
        try {
            rabbitTemplate.convertAndSend(exchange,routingkey, theResult);
        } catch (AmqpException exception) {
            log.error("Error when sending object {} via RabbitMQ routingKey {} : {}", theResult, routingkey, exception.getLocalizedMessage());
            throw exception;
        }
    }

}
