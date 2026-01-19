package vnikolaenko.github.jarch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_files")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProjectFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_id", nullable = false)
    @JsonIgnore
    private Saving saving;
}