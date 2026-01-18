package vnikolaenko.github.user_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;

    //private String role; // например "USER"

    @Column(unique = true, nullable = false)
    private String email;

    public AppUser(String username, String encode, String email) {
        this.username = username;
        this.password = encode;
        this.email = email;
    }
}
