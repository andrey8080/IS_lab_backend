package itmo.andrey.lab_backend.util;

import itmo.andrey.lab_backend.domain.entitie.User;
import itmo.andrey.lab_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseUtil {

    private final UserRepository userRepository;

    @Autowired
    public DatabaseUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
