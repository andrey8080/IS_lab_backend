package itmo.andrey.lab1_backend.model;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Coordinates {
	private BigInteger x; //Значение поля должно быть больше -585
	private BigDecimal y; //Максимальное значение поля: 118, Поле не может быть null

	public BigInteger getX() {
		return x;
	}

	public BigDecimal getY() {
		return y;
	}
}
