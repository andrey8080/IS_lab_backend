package itmo.andrey.lab1_backend.domain.entitie;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Entity
@Table(name = "spacemarine")
@Data
@NoArgsConstructor
public class SpaceMarine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, name="user_name")
	private String userName;

	@Column(nullable = false)
	private BigInteger coordinates_x;

	@Column(nullable = false)
	private BigDecimal coordinates_y;

	@Column(nullable = false)
	private String creationDate;

	@Column(nullable = false)
	private BigInteger health;

	@Column(nullable = false)
	private BigDecimal height;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false)
	private String weaponType;

	@ManyToOne
	@JoinColumn(name = "chapter_id", nullable = false)
	private Chapter chapter;
}
