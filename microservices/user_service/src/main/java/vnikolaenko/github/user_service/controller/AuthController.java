package vnikolaenko.github.user_service.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vnikolaenko.github.user_service.jwt.JwtService;
import vnikolaenko.github.user_service.service.AuthService;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final JwtService jwtService;
    private final AuthService authService;

    /**
     * Пример запроса: GET /register?username=test&password=123
     */
    @GetMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
        String token = authService.register(username, password, email);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    /**
     * Пример запроса: GET /login?username=test&password=123
     */
    @GetMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        String token = authService.login(email, password);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }
}
