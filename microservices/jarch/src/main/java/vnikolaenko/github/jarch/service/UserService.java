package vnikolaenko.github.jarch.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {

    private final RestTemplate restTemplate;

    public UserService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String getUserEmail(String username) {
        try {
            String userServiceUrl = "http://localhost:8090/user/" + username;
            ResponseEntity<String> response = restTemplate.getForEntity(userServiceUrl, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user email: " + e.getMessage());
        }
    }
}