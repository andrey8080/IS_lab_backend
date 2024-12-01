package itmo.andrey.lab1_backend.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import itmo.andrey.lab1_backend.util.EnumUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Обработка ошибок @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Обработка ошибок Enum
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
}
