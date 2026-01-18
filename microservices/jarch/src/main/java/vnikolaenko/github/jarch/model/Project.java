package vnikolaenko.github.jarch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 500)
    private String description;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @OneToMany
    private List<TeamMember> teamMembers;
    @OneToMany
    private List<Saving> savings;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Version
    private Long version;
    @Column
    private String owner;
}
