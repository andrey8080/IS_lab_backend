package itmo.andrey.lab1_backend.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
public class CoordinatesDTO {
    @Min(-585)
    private BigInteger x; //Значение поля должно быть больше -585
    @Max(118)
    private BigDecimal y; //Максимальное значение поля: 118, Поле не может быть null
}