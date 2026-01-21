package vnikolaenko.github.user_service.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vnikolaenko.github.user_service.repository.AppUserRepository;

@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {
    private AppUserRepository appUserRepository;
    @GetMapping("/{username}")
    public ResponseEntity<String> getEmailByUsername(@PathVariable String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(appUserRepository.findByUsername(username).get().getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Такого пользователя не существует.");
        }
    }
    @GetMapping("exist/{username}")
    public ResponseEntity<Boolean> doesUserExist(@PathVariable String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(appUserRepository.findByUsername(username).isPresent());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE);
        }
    }
}
