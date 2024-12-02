package itmo.andrey.lab1_backend.domain.entitie;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "admin_request")
public class AdminRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String reason;
}