package itmo.andrey.lab_backend.repository;

import itmo.andrey.lab_backend.domain.entitie.AdminRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRequestRepository extends JpaRepository<AdminRequest, Long> {
    void deleteAdminRequestById(Long id);

    AdminRequest findByUserId(Long userId);
}
