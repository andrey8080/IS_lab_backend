package itmo.andrey.lab_backend.domain.entitie;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "history_imports")
@Data
@NoArgsConstructor
public class HistoryImports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int counter;

    public String getUsername() {
        return user.getName();
    }
}
