package vnikolaenko.github.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Обрабатываем все исключения одним методом
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleAnyException(Throwable ex) {
        logger.error("Ошибка: {}", ex.getMessage(), ex);

        HttpStatus status = determineHttpStatus(ex);
        String message = getErrorMessage(ex, status);

        return ResponseEntity.status(status).body(message);
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof IllegalArgumentException ||
                ex instanceof IllegalStateException) {
            return HttpStatus.BAD_REQUEST;
        }

        if (ex instanceof NoSuchElementException) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String getErrorMessage(Throwable ex, HttpStatus status) {
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            return "Внутренняя ошибка сервера";
        }

        return ex.getMessage();
    }
}
