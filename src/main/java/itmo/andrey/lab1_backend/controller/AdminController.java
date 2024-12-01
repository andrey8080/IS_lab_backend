package itmo.andrey.lab1_backend.controller;

import itmo.andrey.lab1_backend.domain.entitie.AdminRequest;
import itmo.andrey.lab1_backend.domain.entitie.User;
import itmo.andrey.lab1_backend.repository.AdminRequestRepository;
import itmo.andrey.lab1_backend.repository.UserRepository;
import itmo.andrey.lab1_backend.service.UserCacheService;
import itmo.andrey.lab1_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
@RequestMapping("/admin")
public class AdminController {

    private final AdminRequestRepository adminRequestRepository;
    private final UserService userService;

    private final UserRepository userRepository;
    private final UserCacheService userCacheService;

    public AdminController(AdminRequestRepository adminRequestRepository, UserService userService, UserRepository userRepository, UserCacheService userCacheService) {
        this.adminRequestRepository = adminRequestRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.userCacheService = userCacheService;
    }

    @PostMapping("/approve-admin")
    public ResponseEntity<?> approveAdmin(@RequestParam String username, @RequestHeader("Authorization") String token) {
        boolean validToken = userService.checkValidToken(token);
        if (!validToken) {
            return ResponseEntity.status(401).body("{\"error\":\"Неверный или просроченный токен\"}");
        }

        String adminName = userService.extractUsername(token);
        User admin = userRepository.findByName(adminName);
        if (admin == null) {
            admin = userRepository.findByName(adminName);
        }

        if (admin == null || !admin.isAdmin()) {
            return ResponseEntity.status(403).body("{\"error\":\"Доступ запрещен.\"}");
        }

        User user = userRepository.findByName(username);
        if (user == null) {
            return ResponseEntity.status(404).body("{\"error\":\"Пользователь не найден.\"}");
        }

        user.setAdmin(true);
        userRepository.save(user);

        return ResponseEntity.ok("{\"message\":\"Пользователь назначен администратором.\"}");
    }

    @PostMapping("/admin-requests")
    public ResponseEntity<?> getAdminRequests(@RequestHeader("Authorization") String token) {
        boolean validToken = userService.checkValidToken(token);
        if (!validToken) {
            return ResponseEntity.status(401).body("{\"error\":\"Неверный или просроченный токен\"}");
        }

        String adminName = userService.extractUsername(token);
        User admin = userRepository.findByName(adminName);
        if (admin == null) {
            admin = userRepository.findByName(adminName);
        }

        if (admin == null || !admin.isAdmin()) {
            return ResponseEntity.status(403).body("{\"error\":\"Доступ запрещен.\"}");
        }

        List<AdminRequest> adminRequests = adminRequestRepository.findAll();
        return ResponseEntity.ok(adminRequests);
    }
}
