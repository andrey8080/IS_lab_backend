package itmo.andrey.lab_backend.service;

import itmo.andrey.lab_backend.domain.dto.SpaceMarineDTO;
import itmo.andrey.lab_backend.domain.entitie.Chapter;
import itmo.andrey.lab_backend.domain.entitie.SpaceMarine;
import itmo.andrey.lab_backend.repository.ChapterRepository;
import itmo.andrey.lab_backend.repository.SpaceMarineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class SpaceMarineService {
    private final SpaceMarineRepository spaceMarineRepository;
    private final ChapterRepository chapterRepository;
    private final UserService userService;

    @Autowired
    public SpaceMarineService(SpaceMarineRepository spaceMarineRepository, ChapterRepository chapterRepository, UserService userService) {
        this.spaceMarineRepository = spaceMarineRepository;
        this.chapterRepository = chapterRepository;
        this.userService = userService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean addFromForm(SpaceMarineDTO formData, String userName) {
        synchronized (this) {
            if (userName == null || formData == null) return false;

            Chapter chapter = resolveOrCreateChapter(formData);
            SpaceMarine spaceMarine = buildSpaceMarine(formData, chapter, userName);

            spaceMarineRepository.save(spaceMarine);
            return true;
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SpaceMarine addFromFile(SpaceMarineDTO dto, String userName, LocalDateTime creationDate) {
        Chapter chapter = resolveOrCreateChapterFile(dto, creationDate);
        SpaceMarine spaceMarine = buildSpaceMarine(dto, chapter, userName);
        spaceMarineRepository.save(spaceMarine);
        return spaceMarine;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean updateSpaceMarine(Long id, SpaceMarineDTO formData, String token) {
        SpaceMarine spaceMarine = spaceMarineRepository.findById(id).orElseThrow(() -> new NoSuchElementException("SpaceMarine с ID " + id + " не найден."));

        String userName = userService.extractUsername(token);
        validateUserAccess(spaceMarine, userName);

        updateSpaceMarineFields(spaceMarine, formData);

        Chapter chapter = resolveOrCreateChapter(formData);
        spaceMarine.setChapter(chapter);

        spaceMarineRepository.save(spaceMarine);
        return true;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean deleteSpaceMarine(Long id, String token) {
        SpaceMarine spaceMarine = spaceMarineRepository.findById(id).orElseThrow(() -> new NoSuchElementException("SpaceMarine с ID " + id + " не найден."));

        String userName = userService.extractUsername(token);
        validateUserAccess(spaceMarine, userName);

        spaceMarineRepository.delete(spaceMarine);
        return true;
    }

    public List<SpaceMarine> getAllObjects() {
        return spaceMarineRepository.findAll();
    }

    public List<SpaceMarine> getUserObjects(String userName) {
        return spaceMarineRepository.findByUserName(userName);
    }

    public Optional<SpaceMarine> getSpaceMarineById(long id) {
        return spaceMarineRepository.findById(id);
    }

    private Chapter resolveOrCreateChapter(SpaceMarineDTO formData) {
        String chapterId = formData.getChapter().getId();
        if (chapterId != null && !chapterId.isEmpty()) {
            Chapter chapterFromId = chapterRepository.findById(Long.parseLong(chapterId));
            if (chapterFromId != null) {
                return chapterFromId;
            }
        }

        String chapterName = formData.getChapter().getName();
        Chapter chapterFromName = chapterRepository.findByName(chapterName);

        if (chapterFromName != null) {
            return chapterFromName;
        } else {
            Chapter chapter = createChapter(formData);
            chapterRepository.save(chapter);
            return chapter;
        }
    }

    private Chapter resolveOrCreateChapterFile(SpaceMarineDTO formData, LocalDateTime creationDate) {
        long chapterId = Long.parseLong(formData.getChapter().getId());
        String chapterName = formData.getChapter().getName();
        Chapter chapterFromId = chapterRepository.findById(chapterId);
        Chapter chapterFromName = chapterRepository.findByName(chapterName);

        if (chapterFromId != null) {
            return chapterFromId;
        } else if (chapterFromName != null) {
            return chapterFromName;
        } else {
            Chapter chapter = createChapterWithTimestamp(formData, creationDate);
            chapterRepository.save(chapter);
            return chapter;
        }
    }


    private Chapter createChapter(SpaceMarineDTO formData) {
        Chapter chapter = new Chapter();
        chapter.setName(formData.getChapter().getName());
        chapter.setCount(formData.getChapter().getMarinesCount());
        chapter.setWorld(formData.getChapter().getWorld());
        chapterRepository.save(chapter);
        return chapter;
    }

    private Chapter createChapterWithTimestamp(SpaceMarineDTO formData, LocalDateTime creationDate) {
        String timestamp = creationDate.format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss"));
        Chapter chapter = new Chapter();
        chapter.setName(formData.getChapter().getName() + "_" + timestamp);
        chapter.setCount(formData.getChapter().getMarinesCount());
        chapter.setWorld(formData.getChapter().getWorld());
        return chapter;
    }

    private SpaceMarine buildSpaceMarine(SpaceMarineDTO formData, Chapter chapter, String userName) {
        SpaceMarine spaceMarine = new SpaceMarine();
        spaceMarine.setName(formData.getName());
        spaceMarine.setCoordinates_x(formData.getCoordinates().getX());
        spaceMarine.setCoordinates_y(formData.getCoordinates().getY());
        spaceMarine.setCreationDate(LocalDateTime.now().toString());
        spaceMarine.setHealth(formData.getHealth());
        spaceMarine.setHeight(formData.getHeight());
        spaceMarine.setCategory(formData.getCategory());
        spaceMarine.setWeaponType(formData.getWeaponType());
        spaceMarine.setUserName(userName);
        spaceMarine.setChapter(chapter);
        return spaceMarine;
    }

    private void updateSpaceMarineFields(SpaceMarine spaceMarine, SpaceMarineDTO formData) {
        spaceMarine.setName(formData.getName());
        spaceMarine.setCoordinates_x(formData.getCoordinates().getX());
        spaceMarine.setCoordinates_y(formData.getCoordinates().getY());
        spaceMarine.setHealth(formData.getHealth());
        spaceMarine.setHeight(formData.getHeight());
        spaceMarine.setCategory(formData.getCategory());
        spaceMarine.setWeaponType(formData.getWeaponType());
    }

    private void validateUserAccess(SpaceMarine spaceMarine, String userName) {
        if (!spaceMarine.getUserName().equals(userName) && userService.getUserRole(userName).equals("user")) {
            throw new SecurityException("Нет прав для выполнения операции.");
        }
    }
}
