package itmo.andrey.lab1_backend.repository;

import itmo.andrey.lab1_backend.domain.entitie.Chapter;
import itmo.andrey.lab1_backend.domain.entitie.SpaceMarine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceMarineRepository extends JpaRepository<SpaceMarine, Long> {
	List<SpaceMarine> findByUserName(String user_name);
	SpaceMarine findSpaceMarineById(Long id);

	List<SpaceMarine> findByChapter(Chapter chapter);
}
