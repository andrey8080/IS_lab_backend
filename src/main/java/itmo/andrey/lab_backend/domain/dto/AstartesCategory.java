package itmo.andrey.lab_backend.domain.dto;

import lombok.Getter;

@Getter
public enum AstartesCategory {
	SCOUT("Скаут"),
	AGGRESSOR("Агрессор"),
	INCEPTOR("Инцептор"),
	SUPPRESSOR("Супрессор"),
	TERMINATOR("Терминатор");

	private final String category;

	AstartesCategory(String category) {
		this.category = category;
	}

	public static AstartesCategory fromString(String category) {
		for (AstartesCategory c : AstartesCategory.values()) {
			if (c.category.equalsIgnoreCase(category)) {
				return c;
			}
		}
		throw new IllegalArgumentException("No enum constant " + AstartesCategory.class.getCanonicalName() + "." + category);
	}
}