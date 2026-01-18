package vnikolaenko.github.notification_app.rabbit;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import vnikolaenko.github.notification_app.EmailService;
import vnikolaenko.github.network.rabbit.UserMessage;

@Service
@Slf4j
@AllArgsConstructor
public class RabbitMQConsumer {
    private final EmailService emailService;

    /**
     * Получить уведомление
     */
    @RabbitListener(queues = "email.send")
    public void receiveNotification(UserMessage message) {
        log.info("📥 Получено уведомление для пользователя: {}", message.getEmail());
        emailService.sendSimpleHtmlEmail(message.getEmail(), "Уведомление от JARCH", message.getAction());
    }
}
