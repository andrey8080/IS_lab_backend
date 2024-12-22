package itmo.andrey.lab_backend.repository;

import itmo.andrey.lab_backend.domain.entitie.HistoryImports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryImportsRepository extends JpaRepository<HistoryImports, Long> {
    List<HistoryImports> findAll();

    List<HistoryImports> getAllByUser_Name(String userName);
}
