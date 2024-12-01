package itmo.andrey.lab1_backend.controller;

import itmo.andrey.lab1_backend.domain.SignUpRequest;
import itmo.andrey.lab1_backend.domain.dto.AdminRequestDTO;
import itmo.andrey.lab1_backend.domain.dto.SignInDTO;
import itmo.andrey.lab1_backend.domain.dto.SignUpDTO;
import itmo.andrey.lab1_backend.domain.entitie.AdminRequest;
import itmo.andrey.lab1_backend.domain.entitie.User;
import itmo.andrey.lab1_backend.repository.AdminRequestRepository;
import itmo.andrey.lab1_backend.repository.UserRepository;
import itmo.andrey.lab1_backend.service.UserCacheService;
import itmo.andrey.lab1_backend.service.UserService;
import itmo.andrey.lab1_backend.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
@RequestMapping("/auth")
public class AuthController {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final AdminRequestRepository adminRequestRepository;
    private final UserCacheService userCacheService;
    private final UserService userService;

    @Autowired
    public AuthController(JwtTokenUtil jwtTokenUtil, UserRepository userRepository, AdminRequestRepository adminRequestRepository, UserService userService, UserCacheService userCacheService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.userCacheService = userCacheService;
        this.adminRequestRepository = adminRequestRepository;
        this.userService = userService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signinForm(@RequestBody SignInDTO formData) {
        User user = userCacheService.getUserFromCache(formData.getName());

        if (user == null) {
            user = userRepository.findByName(formData.getName());
            if (user != null) {
                userCacheService.cacheUser(user);
            }
        }

        if (user != null && user.getPassword().equals(formData.getPassword())) {
            String jwtToken = jwtTokenUtil.generateJwtToken(user.getName());
            return ResponseEntity.ok("{\"token\":\"" + jwtToken + "\"}");
        } else {
            return ResponseEntity.status(401).body("{\"error\":\"Invalid credentials\"}");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signupForm(@RequestBody SignUpRequest signUpRequest) {
        SignUpDTO formData = signUpRequest.getFormData();
        AdminRequestDTO adminRequestDTO = signUpRequest.getAdminRequestData();

        if (formData.getName() == null || formData.getName().isEmpty()) {
            return ResponseEntity.status(400).body("{\"error\":\"Имя пользователя не может быть пустым.\"}");
        }

        List<User> allUsers = userRepository.findAll();
        boolean isFirstAdmin = allUsers.stream().noneMatch(User::isAdmin);

        User newUser = new User(formData.getName(), formData.getPassword());

        if (userRepository.existsByName(newUser.getName())) {
            return ResponseEntity.status(409).body("{\"error\":\"Логин занят. Попробуйте другой.\"}");
        }

        if (isFirstAdmin) {
            newUser.setAdmin(true);
        } else if (formData.isCandidateAdmin()) {
            newUser.setAdmin(false);
            AdminRequest newAdminRequest = new AdminRequest();
            newAdminRequest.setUser(newUser);
            newAdminRequest.setReason(adminRequestDTO.getReason());
            adminRequestRepository.save(newAdminRequest);
        }

        userRepository.save(newUser);
        userCacheService.cacheUser(newUser);

        String jwtToken = jwtTokenUtil.generateJwtToken(newUser.getName());
        return ResponseEntity.ok("{\"token\":\"" + jwtToken + "\"}");
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> checkToken(@RequestHeader("Authorization") String token) {
        String role = userService.getUserRole(userService.extractUsername(token));
        if (role == null) {
            return ResponseEntity.status(401).body("{\"error\":\"Неверный или просроченный токен\"}");
        }
        return ResponseEntity.ok("{\"role\":\"" + role + "\"}");
    }
}
