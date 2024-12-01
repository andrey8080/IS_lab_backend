package itmo.andrey.lab1_backend.domain.entitie;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chapter")
@Data
@NoArgsConstructor
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(nullable = false)
    private String count;
    @Column()
    private String world;

    public Chapter(String name, String count, String world) {
        this.name = name;
        this.count = count;
        this.world = world;
    }
}
