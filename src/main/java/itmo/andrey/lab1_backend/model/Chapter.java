package itmo.andrey.lab1_backend.model;

import java.math.BigDecimal;

public class Chapter {
	private String name; //Поле не может быть null, Строка не может быть пустой
	private BigDecimal marinesCount; //Поле не может быть null, Значение поля должно быть больше 0, Максимальное значение поля: 1000
	private String world; //Поле может быть null

	public String getName() {
		return name;
	}

	public BigDecimal getMarinesCount() {
		return marinesCount;
	}

	public String getWorld() {
		return world;
	}
}