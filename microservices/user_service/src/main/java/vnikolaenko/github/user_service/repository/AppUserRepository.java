package vnikolaenko.github.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vnikolaenko.github.user_service.model.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);

    String username(String username);
}
