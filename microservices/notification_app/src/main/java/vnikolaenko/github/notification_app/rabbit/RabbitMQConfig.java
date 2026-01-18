package vnikolaenko.github.notification_app.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        // Явно указываем использовать JSON
        converter.setClassMapper(classMapper());
        converter.setCreateMessageIds(true); // для отслеживания
        return converter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages(
                "vnikolaenko.github.network.rabbit",
                "java.util",
                "java.time"
        );
        // Или разрешить все классы (только для разработки!)
        // classMapper.setTrustedPackages("*");
        return classMapper;
    }

    // Обменники (Exchanges)
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("email.exchange");
    }

    // Очереди (Queues)
    @Bean
    public Queue emailQueue() {
        return new Queue("email.send", true); // true = durable очередь
    }

    // Привязки (Bindings)
    @Bean
    public Binding binding(Queue emailQueue, DirectExchange exchange) {
        return BindingBuilder.bind(emailQueue)
                .to(exchange)
                .with("email.routing.key");
    }
}