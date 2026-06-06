package com.ms.user.producers;

import com.ms.user.dtos.EmailDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${broker.queue.email.name}")
    private String routingKey;

    public void publishMessageEmail(EmailDto emailDto) {
        // Envia o DTO transformado em JSON para a fila default.email
        rabbitTemplate.convertAndSend("", routingKey, emailDto);
    }
}