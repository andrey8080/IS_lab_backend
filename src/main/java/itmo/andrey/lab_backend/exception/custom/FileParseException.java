package itmo.andrey.lab_backend.exception.custom;

public class FileParseException extends RuntimeException {
    public FileParseException(String message) {
        super(message);
    }
}