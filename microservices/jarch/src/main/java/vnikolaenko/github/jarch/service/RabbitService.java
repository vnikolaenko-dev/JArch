package vnikolaenko.github.jarch.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vnikolaenko.github.network.rabbit.UserMessage;

@Service
@Slf4j
@AllArgsConstructor
public class RabbitService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Отправить сообщение пользователю
     */
    public void sendUserMessage(UserMessage userMessage) {
        try {
            rabbitTemplate.convertAndSend("email.exchange", "email.routing.key", userMessage);
            log.info("✅ Сообщение отправлено пользователю отправлено: {}", userMessage.getEmail());
        } catch (Exception e) {
            log.error("❌ Ошибка отправки сообщения о пользователе: {}", e.getMessage());
        }
    }
}
