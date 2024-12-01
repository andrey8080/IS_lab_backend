package itmo.andrey.lab1_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class SpaceMarineDTO {

	@NotBlank(message = "Имя не может быть пустым")
	@NotNull(message = "Имя не может быть null")
	private String name;

	@NotNull(message = "Координаты не могут быть null")
	private CoordinatesDTO coordinates;

	@NotNull(message = "Chapter не может быть null")
	private ChapterDTO chapter;

	@NotNull(message = "Health не может быть null")
	@Positive(message = "Health должен быть больше 0")
	private BigInteger health;

	private BigDecimal height;

	@NotNull(message = "Category не может быть null")
	private AstartesCategory category;

	@NotNull(message = "WeaponType не может быть null")
	private WeaponType weaponType;


	public String getCategory() {
		return category.getCategory();
	}

	public String getWeaponType() {
		return weaponType.getType();
	}
}

@Getter
enum WeaponType {
	HEAVY_BOLTGUN ("Тяжёлый болтовой пистолет"),
	BOLT_PISTOL ("Болтовой пистолет"),
	BOLT_RIFLE ("Болтовая винтовка"),
	COMBI_FLAMER ("Комби огнемёт"),
	GRAVY_GUN ("Гравипушка");

	private final String type;
	private WeaponType(String type) {
		this.type = type;
	}
}

@Getter
enum AstartesCategory {
	SCOUT ("Скаут"),
	AGGRESSOR ("Агрессор"),
	INCEPTOR ("Инцептор"),
	SUPPRESSOR ("Супрессор"),
	TERMINATOR ("Терминатор");

	private final String category;
	private AstartesCategory(String category) {
		this.category = category;
	}
}