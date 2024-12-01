package itmo.andrey.lab1_backend.controller;

import itmo.andrey.lab1_backend.domain.dto.SpaceMarineDTO;
import itmo.andrey.lab1_backend.service.SpaceMarineService;
import itmo.andrey.lab1_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/space-marine")
public class SpaceMarineController {

    private final SpaceMarineService spaceMarineService;
    private final UserService userService;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public SpaceMarineController(SimpMessagingTemplate messagingTemplate, SpaceMarineService spaceMarineService, UserService userService) {
        this.spaceMarineService = spaceMarineService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@Valid @RequestBody SpaceMarineDTO formData, @RequestHeader("Authorization") String token) {
        boolean validToken = userService.checkValidToken(token);


        if (validToken) {
            try {
                boolean added = spaceMarineService.add(formData, userService.extractUsername(token));
                if (added) {
                    sendUpdateMessage("add");
                    return ResponseEntity.ok().body("{\"message\":\"SpaceMarine успешно добавлен\"}");
                } else {
                    return ResponseEntity.status(400).body("{\"error\": \"Ошибка при добавлении SpaceMarine\"}");
                }

            } catch (DataIntegrityViolationException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\":\"Орден с таким именем уже существует. Пожалуйста, выберите другое имя.\"}");
            } catch (Exception e) {
                return ResponseEntity.status(400).body("{\"error\":\"" + e.getMessage() + "\"}");
            }
        } else {
            return ResponseEntity.status(401).body("{\"error\":\"Неверный или просроченный токен\"}");
        }
    }

    @PostMapping("get/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
        boolean validToken = userService.checkValidToken(token);
        if (!validToken) {
            return ResponseEntity.status(401).body("{\"error\":\"Неверный или просроченный токен\"}");
        }
        return ResponseEntity.ok().body(spaceMarineService.getSpaceMarineById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SpaceMarineDTO formData,
                                    @RequestHeader("Authorization") String token) {

        boolean validToken = userService.checkValidToken(token);
        if (!validToken) {
            return ResponseEntity.status(401).body("{\"error\":\"Неверный или просроченный токен\"}");
        }

        try {
            boolean updated = spaceMarineService.updateSpaceMarine(id, formData, token);
            if (updated) {
                sendUpdateMessage("update");
                return ResponseEntity.ok().body("{\"message\":\"SpaceMarine успешно обновлен\"}");
            } else {
                return ResponseEntity.status(404).body("{\"error\":\"SpaceMarine не найден\"}");
            }
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\":\"Орден с таким именем уже существует. Пожалуйста, выберите другое имя.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSpaceMarine(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        boolean validToken = userService.checkValidToken(token);
        if (!validToken) {
            return ResponseEntity.status(401).body("{\"error\":\"Неверный или просроченный токен\"}");
        }

        try {
            boolean result = spaceMarineService.deleteSpaceMarine(id, token);
            if (result) {
                return ResponseEntity.ok("{\"message\":\"SpaceMarine успешно удален\"}");
            } else {
                return ResponseEntity.status(404).body("{\"error\":\"SpaceMarine не найден\"}");
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/all-objects")
    public ResponseEntity<?> getAllObjects() {
        return ResponseEntity.ok(spaceMarineService.getAllObjects());
    }

    @PostMapping("/user-objects")
    public ResponseEntity<?> getUserObjects(@RequestHeader("Authorization") String token) {
        boolean validToken = userService.checkValidToken(token);

        if (validToken) {
            return ResponseEntity.ok().body(spaceMarineService.getUserObjects(userService.extractUsername(token)));
        } else {
            return ResponseEntity.status(401).body("{\"error\":\"Неверный или просроченный токен\"}");
        }
    }

    public void sendUpdateMessage(String message) {
        messagingTemplate.convertAndSend("/topic/updates", message);
    }

}
