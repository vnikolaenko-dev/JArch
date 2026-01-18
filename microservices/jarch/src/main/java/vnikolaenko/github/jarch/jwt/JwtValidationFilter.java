package vnikolaenko.github.jarch.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);

            if (jwtService.isTokenValid(jwt)) {
                String username = jwtService.extractUsername(jwt);

                // Создаем простую аутентификацию на основе JWT
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                // Извлекаем роли из JWT claims
                                extractAuthorities(jwt)
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Authenticated user from JWT: {}", username);
            }

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private Collection<? extends GrantedAuthority> extractAuthorities(String jwt) {
        // Извлекаем роли из JWT claims
        List<String> roles = jwtService.extractClaim(jwt, claims ->
                claims.get("roles", List.class));

        if (roles == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}