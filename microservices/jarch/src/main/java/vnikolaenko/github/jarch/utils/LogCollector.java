package vnikolaenko.github.jarch.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Service
public class LogCollector {
    private SseEmitter emitter;

    public LogCollector() {
        this.emitter = new SseEmitter();
    }

    public LogCollector(SseEmitter emitter) {
        this.emitter = new SseEmitter();
    }

    public void info(String message) {
        sendLog("INFO", message);
    }

    public void warn(String message) {
        sendLog("WARN", message);
    }

    public void error(String message) {
        sendLog("ERROR", message);
    }

    private void sendLog(String level, String message) {
        try {
            Map<String, String> logEvent = new HashMap<>();
            logEvent.put("level", level);
            logEvent.put("message", message);
            logEvent.put("timestamp", Instant.now().toString());

            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("log")
                    .data(logEvent);
            emitter.send(event);
        } catch (IOException e) {
            System.err.println("Ошибка отправки лога: " + e.getMessage());
        }
    }

}
