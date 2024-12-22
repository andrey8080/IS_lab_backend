package itmo.andrey.lab_backend.controller;

import itmo.andrey.lab_backend.domain.entitie.HistoryImports;
import itmo.andrey.lab_backend.service.FileService;
import itmo.andrey.lab_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> uploadFile(@RequestHeader("Authorization") String token) {
        boolean validToken = userService.checkValidToken(token);

        if (validToken) {
            fileService.uploadFile();
            return null;
        } else {
            return ResponseEntity.status(400).body("{\"error\":\"Ошибка обработки токена\"}");
        }
    }

    @PostMapping("/getHistory")
    public ResponseEntity<?> getHistory(@RequestHeader("Authorization") String token) {
        boolean validToken = userService.checkValidToken(token);

        if (validToken) {
            List<HistoryImports> historyImportsList = fileService.getHistory(userService.getUserRole(token), userService.extractUsername(token));
            return ResponseEntity.ok(historyImportsList);
        } else {
            return ResponseEntity.status(400).body("{\"error\":\"Ошибка обработки токена\"}");
        }
    }
}
