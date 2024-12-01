package itmo.andrey.lab1_backend.repository;

import itmo.andrey.lab1_backend.domain.entitie.AdminRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRequestRepository extends JpaRepository<AdminRequest, Long> {
}
