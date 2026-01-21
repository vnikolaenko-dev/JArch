package vnikolaenko.github.notification_app;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;



    public void sendHtmlEmail(String to, String subject, String text) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("viktor.nikolaenko.2005@gmail.com");

        // Подготавливаем данные для шаблона
        Context context = new Context();
        context.setVariable("subject", subject);
        context.setVariable("text", text);
        context.setVariable("buttonText", "Открыть");
        context.setVariable("buttonLink", "http://localhost:3000/");

        // Генерируем HTML из шаблона
        String htmlContent = templateEngine.process("email/template", context);

        helper.setText(htmlContent, true);

        // Для альтернативного текстового представления
        helper.setText(text, htmlContent);

        mailSender.send(message);
    }

    // Упрощенный метод как в вашем примере
    public void sendSimpleHtmlEmail(String to, String subject, String text) {
        try {
            sendHtmlEmail(to, subject, text);
        } catch (MessagingException e) {
            // Обработка ошибки
            e.printStackTrace();
        }
    }
}
