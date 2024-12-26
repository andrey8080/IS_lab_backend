package itmo.andrey.lab_backend.controller;

import itmo.andrey.lab_backend.domain.dto.SpaceMarineDTO;
import itmo.andrey.lab_backend.exception.custom.ChapterAlreadyExistsException;
import itmo.andrey.lab_backend.service.FileService;
import itmo.andrey.lab_backend.service.SpaceMarineService;
import itmo.andrey.lab_backend.service.UserService;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/file")
public class FileController {
    private final UserService userService;
    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestHeader("Authorization") String token,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "fileCreationDate", required = false) String fileCreationDate) {
        boolean validToken = userService.checkValidToken(token);

        if (!validToken) {
            return ResponseEntity.status(400).body("{\"error\":\"Invalid token\"}");
        }

        String userName = userService.extractUsername(token);

        try {
            LocalDateTime creationDate;
            if (fileCreationDate != null) {
                try {
                    Instant instant = Instant.parse(fileCreationDate);
                    creationDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                } catch (DateTimeParseException e) {
                    return ResponseEntity.status(400).body("{\"error\":\"Invalid fileCreationDate format\"}");
                }
            } else {
                // Если дата не передана, используем текущую
                creationDate = LocalDateTime.now();
            }

            List<SpaceMarineDTO> spaceMarineDTOList = fileService.uploadFile(file, userName, creationDate);

            if (spaceMarineDTOList == null) {
                return ResponseEntity.status(400).body("{\"error\":\"Import failed due to invalid data or conflicts\"}");
            }
            return ResponseEntity.ok(fileService.getHistory(token));
        } catch (ChapterAlreadyExistsException ex) {
            throw ex;
        } catch (ConstraintViolationException | DataIntegrityViolationException ex) {
            System.out.println(ex.toString());
            return ResponseEntity.status(409).body("{\"error\":\"Проверьте данные в файле\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\":\"Internal server error\"}");
        }
    }

    @PostMapping("/getHistory")
    public ResponseEntity<?> getHistory(@RequestHeader("Authorization") String token) {
        boolean validToken = userService.checkValidToken(token);

        if (validToken) {
            return ResponseEntity.ok(fileService.getHistory(token));
        } else {
            return ResponseEntity.status(400).body("{\"error\":\"Ошибка обработки токена\"}");
        }
    }
}