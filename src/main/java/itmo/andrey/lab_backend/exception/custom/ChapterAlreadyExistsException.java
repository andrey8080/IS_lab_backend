package itmo.andrey.lab_backend.exception.custom;

public class ChapterAlreadyExistsException extends RuntimeException {
    public ChapterAlreadyExistsException() {
        super("Имя ордена уже существует");
    }
    public ChapterAlreadyExistsException(String message) {
        super(message);
    }
}
