package vnikolaenko.github.jarch.jwt;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SimpleUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Для демо - создаем пользователя на основе JWT
        // В реальном микросервисе здесь может быть вызов User Service
        return User.builder()
                .username(username)
                .password("") // пароль не используется в JWT
                .authorities("ROLE_USER") // базовые права
                .build();
    }
}
