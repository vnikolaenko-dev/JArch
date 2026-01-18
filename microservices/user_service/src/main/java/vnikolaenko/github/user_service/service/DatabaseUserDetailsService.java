package vnikolaenko.github.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vnikolaenko.github.user_service.model.AppUser;
import vnikolaenko.github.user_service.repository.AppUserRepository;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                // .roles(user.getRole()) // роли берутся из поля role
                .build();
    }
}

