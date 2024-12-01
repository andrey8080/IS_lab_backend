package itmo.andrey.lab1_backend.repository;

import itmo.andrey.lab1_backend.domain.entitie.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	User findByName(String name);
	boolean existsByName(String name);
}
