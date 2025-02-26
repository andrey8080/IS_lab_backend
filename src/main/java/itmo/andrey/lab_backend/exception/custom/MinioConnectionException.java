package itmo.andrey.lab_backend.exception.custom;

public class MinioConnectionException extends RuntimeException {
    public MinioConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}