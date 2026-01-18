package vnikolaenko.github.user_service.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vnikolaenko.github.user_service.jwt.JwtService;
import vnikolaenko.github.user_service.model.AppUser;
import vnikolaenko.github.user_service.repository.AppUserRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {
    private PasswordEncoder bCryptPasswordEncoder;
    private AppUserRepository appUserRepository;
    private JwtService jwtService;

    public String register(String username, String password, String email) {
        if (appUserRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username is already in use");
        }
        appUserRepository.save(new AppUser(username, bCryptPasswordEncoder.encode(password), email));

        return jwtService.generateToken(
                User.builder()
                        .username(username)
                        .password(bCryptPasswordEncoder.encode(password))
                        .roles("USER")
                        .build(), email);
    }

    public String login(String email, String password) {
        Optional<AppUser> appUser = appUserRepository.findByEmail(email);
        if (appUser.isEmpty()) {
            throw new RuntimeException("Username incorrect");
        }
        if (!bCryptPasswordEncoder.matches(password, appUser.get().getPassword())) {
            throw new RuntimeException("Password incorrect");
        }
        return jwtService.generateToken(
                User.builder()
                        .username(appUser.get().getUsername())
                        .password(bCryptPasswordEncoder.encode(password))
                        .roles("USER")
                        .build(), email);
    }
}
