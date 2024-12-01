package itmo.andrey.lab1_backend.domain.dto;

import lombok.Data;

@Data
public class ChapterDTO {
    private String id;
    private String name; //Поле не может быть null, Строка не может быть пустой
    private String marinesCount; //Поле не может быть null, Значение поля должно быть больше 0, Максимальное значение поля: 1000
    private String world; //Поле может быть null
}
