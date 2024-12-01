package itmo.andrey.lab1_backend.model;

public class SpaceMarine {
	private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
	private String name; //Поле не может быть null, Строка не может быть пустой
	private Coordinates coordinates; //Поле не может быть null
	private java.time.LocalDate creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
	private Chapter chapter; //Поле не может быть null
	private int health; //Значение поля должно быть больше 0
	private long height;
	private AstartesCategory category; //Поле не может быть null
	private Weapon weaponType; //Поле не может быть null
}
