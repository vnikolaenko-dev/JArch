package vnikolaenko.github.jarch.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    /**
     * Получает username текущего аутентифицированного пользователя
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }

    /**
     * Безопасное получение username (возвращает null если не аутентифицирован)
     */
    public String getCurrentUsernameSafe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Проверяет, имеет ли текущий пользователь указанную роль
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(roleWithPrefix));
    }

    /**
     * Проверяет, аутентифицирован ли текущий пользователь
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Получает UserDetails текущего пользователя
     */
    public UserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        return null;
    }

    /**
     * Проверяет, имеет ли пользователь любую из указанных ролей
     */
    public boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> {
                    for (String role : roles) {
                        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        if (authority.equals(roleWithPrefix)) {
                            return true;
                        }
                    }
                    return false;
                });
    }
}
