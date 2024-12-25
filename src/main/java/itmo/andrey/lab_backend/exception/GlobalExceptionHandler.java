package itmo.andrey.lab_backend.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import itmo.andrey.lab_backend.exception.custom.FileParseException;
import itmo.andrey.lab_backend.exception.custom.ChapterAlreadyExistsException;
import itmo.andrey.lab_backend.exception.custom.UserNotFoundException;
import itmo.andrey.lab_backend.util.EnumUtil;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Map<String, String>> handleEnumValidationExceptions(InvalidFormatException ex) {
        String invalidValue = ex.getValue().toString();
        Class<?> enumType = ex.getTargetType();

        if (enumType.isEnum()) {
            String[] validValues = EnumUtil.getEnumValues(enumType);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", String.format("Недопустимое значение: '%s'. Допустимые значения: %s",
                    invalidValue, String.join(", ", validValues)));
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Ошибка при обработке запроса");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ChapterAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleChapterAlreadyExistsException(ChapterAlreadyExistsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileParseException.class)
    public ResponseEntity<Map<String, String>> handleFileParseException(FileParseException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Произошла ошибка на сервере");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Ошибка при обработке запроса. Проверьте содержимое файла на корректность");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Ошибка при обработке запроса. Проверьте содержимое файла на корректность");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
