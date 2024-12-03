package itmo.andrey.lab1_backend.controller;

import itmo.andrey.lab1_backend.domain.dto.AdminRequestDTO;
import itmo.andrey.lab1_backend.domain.dto.AdminResponseDTO;
import itmo.andrey.lab1_backend.domain.entitie.AdminRequest;
import itmo.andrey.lab1_backend.domain.entitie.User;
import itmo.andrey.lab1_backend.repository.AdminRequestRepository;
import itmo.andrey.lab1_backend.repository.UserRepository;
import itmo.andrey.lab1_backend.service.UserCacheService;
import itmo.andrey.lab1_backend.service.UserService;
import itmo.andrey.lab1_backend.util.JwtTokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:80"}, allowedHeaders = "*")
@RequestMapping("/admin")
public class AdminController {

    private final AdminRequestRepository adminRequestRepository;
    private final UserService userService;
    private final UserCacheService userCacheService;

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public AdminController(AdminRequestRepository adminRequestRepository, UserService userService, UserCacheService userCacheService, UserRepository userRepository, JwtTokenUtil jwtTokenUtil) {
        this.adminRequestRepository = adminRequestRepository;
        this.userService = userService;
        this.userCacheService = userCacheService;
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> adminSignup(@RequestBody AdminRequestDTO formData) {
        if (formData.getName() == null || formData.getName().isEmpty()) {
            return ResponseEntity.status(400).body("{\"error\":\"Имя пользователя (" + formData.getName() + ") не может быть пустым.\"}");
        }

        List<User> allUsers = userRepository.findAll();
        boolean isFirstAdmin = allUsers.stream().noneMatch(User::isAdmin);

        User newUser = new User(formData.getName(), formData.getPassword());
        AdminRequest newAdminRequest = null;
        if (userRepository.existsByName(newUser.getName())) {
            return ResponseEntity.status(409).body("{\"error\":\"Логин занят. Попробуйте другой.\"}");
        }

        if (isFirstAdmin) {
            newUser.setAdmin(true);
        } else {
            newUser.setAdmin(false);
            newAdminRequest = new AdminRequest();
            newAdminRequest.setUser(newUser);
            newAdminRequest.setReason(formData.getReason());
        }

        userRepository.save(newUser);
        userCacheService.cacheUser(newUser);
        if (newAdminRequest != null) adminRequestRepository.save(newAdminRequest);

        String jwtToken = jwtTokenUtil.generateJwtToken(newUser.getName());
        return ResponseEntity.ok("{\"token\":\"" + jwtToken + "\"}");
    }

    @PostMapping("/admin-requests")
    public ResponseEntity<?> getAdminRequests(@RequestHeader("Authorization") String token) {
        boolean validToken = userService.checkValidToken(token);
        if (!validToken) {
            return ResponseEntity.status(401).body("{\"error\":\"Неверный или просроченный токен\"}");
        }

        String adminName = userService.extractUsername(token);
        User admin = userRepository.findByName(adminName);
        if (admin == null || !admin.isAdmin()) {
            return ResponseEntity.status(403).body("{\"error\":\"Доступ запрещен.\"}");
        }

        List<AdminRequest> adminRequests = adminRequestRepository.findAll();
        List<AdminResponseDTO> response = adminRequests.stream()
                .map(request -> new AdminResponseDTO(request.getUser().getName(), request.getReason(), false))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve-admin")
    @Transactional
    public ResponseEntity<?> approveAdmin(@RequestHeader("Authorization") String token, @RequestBody AdminResponseDTO responseDTO) {
        boolean validToken = userService.checkValidToken(token);
        if (!validToken) {
            return ResponseEntity.status(401).body("{\"error\":\"Неверный или просроченный токен\"}");
        }

        String adminName = userService.extractUsername(token);
        User admin = userRepository.findByName(adminName);
        if (admin == null || !admin.isAdmin()) {
            return ResponseEntity.status(403).body("{\"error\":\"Доступ запрещен.\"}");
        }

        User user = userRepository.findByName(responseDTO.getUsername());
        if (user == null) {
            return ResponseEntity.status(404).body("{\"error\":\"Пользователь не найден.\"}");
        }

        AdminRequest adminRequest = adminRequestRepository.findByUserId(user.getId());
        if (adminRequest == null) {
            return ResponseEntity.status(404).body("{\"error\":\"Заявка не найдена.\"}");
        }

        if (responseDTO.getIsApproved()) {
            user.setAdmin(true);
            userRepository.save(user);
        }

        adminRequestRepository.deleteAdminRequestById(adminRequest.getId());
        return ResponseEntity.ok("{\"message\":\"Заявка обработана.\"}");
    }
}
