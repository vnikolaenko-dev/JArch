package vnikolaenko.github.jarch.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
        String message = ex.getMessage();

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

        if (ex instanceof AccessDeniedException ||
                ex instanceof SecurityException) {
            return HttpStatus.FORBIDDEN;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
