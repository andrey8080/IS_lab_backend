package itmo.andrey.lab_backend.domain.dto;

import lombok.Getter;

@Getter
public enum WeaponType {
	HEAVY_BOLTGUN("Тяжёлый болтовой пистолет"),
	BOLT_PISTOL("Болтовой пистолет"),
	BOLT_RIFLE("Болтовая винтовка"),
	COMBI_FLAMER("Комби огнемёт"),
	GRAVY_GUN("Гравипушка");

	private final String type;

	WeaponType(String type) {
		this.type = type;
	}

	public static WeaponType fromString(String text) {
		for (WeaponType b : WeaponType.values()) {
			if (b.type.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}