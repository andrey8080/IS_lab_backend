package itmo.andrey.lab1_backend.repository;

import itmo.andrey.lab1_backend.domain.entitie.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    Chapter findById(long id);
}
