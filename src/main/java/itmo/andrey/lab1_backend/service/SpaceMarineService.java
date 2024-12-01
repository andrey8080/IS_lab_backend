package itmo.andrey.lab1_backend.service;

import itmo.andrey.lab1_backend.domain.dto.SpaceMarineDTO;
import itmo.andrey.lab1_backend.domain.entitie.Chapter;
import itmo.andrey.lab1_backend.domain.entitie.SpaceMarine;
import itmo.andrey.lab1_backend.repository.ChapterRepository;
import itmo.andrey.lab1_backend.repository.SpaceMarineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
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

    public boolean add(SpaceMarineDTO formData, String userName) {
        SpaceMarine spaceMarine = new SpaceMarine();
        spaceMarine.setName(formData.getName());
        spaceMarine.setCoordinates_x(formData.getCoordinates().getX());
        spaceMarine.setCoordinates_y(formData.getCoordinates().getY());
        spaceMarine.setCreationDate(String.valueOf(java.time.LocalDateTime.now()));
        spaceMarine.setHealth(formData.getHealth());
        spaceMarine.setHeight(formData.getHeight());
        spaceMarine.setCategory(formData.getCategory());
        spaceMarine.setWeaponType(formData.getWeaponType());

        if (userName != null) {
            spaceMarine.setUserName(userName);
        } else {
            return false;
        }
        String idChapter = formData.getChapter().getId();
        if (!Objects.equals(idChapter, "")) {
            spaceMarine.setChapter(chapterRepository.findById(Long.parseLong(idChapter)));
        } else {
            Chapter newChapter = new Chapter(formData.getChapter().getName(), formData.getChapter().getMarinesCount(), formData.getChapter().getWorld());
            chapterRepository.save(newChapter);
            spaceMarine.setChapter(newChapter);
        }

        spaceMarineRepository.save(spaceMarine);
        return true;
    }

    public boolean updateSpaceMarine(Long id, SpaceMarineDTO formData, String token) {
        Optional<SpaceMarine> optionalSpaceMarine = spaceMarineRepository.findById(id);
        if (optionalSpaceMarine.isEmpty()) {
            return false;
        }

        SpaceMarine spaceMarine = optionalSpaceMarine.get();
        String userName = userService.extractUsername(token);
        if (userService.getUserRole(userName).equals("user") && !spaceMarine.getUserName().equals(userName)) {
            throw new SecurityException("Нет прав на удаление этого объекта");
        }

        if (formData.getName() == null || formData.getCategory() == null) {
            throw new IllegalArgumentException("Обязательные поля отсутствуют");
        }

        spaceMarine.setName(formData.getName());
        spaceMarine.setCoordinates_x(formData.getCoordinates().getX());
        spaceMarine.setCoordinates_y(formData.getCoordinates().getY());
        spaceMarine.setHealth(formData.getHealth());
        spaceMarine.setHeight(formData.getHeight());
        spaceMarine.setCategory(formData.getCategory());
        spaceMarine.setWeaponType(formData.getWeaponType());

        String idChapter = formData.getChapter().getId();
        if (!Objects.equals(idChapter, "")) {
            spaceMarine.setChapter(chapterRepository.findById(Long.parseLong(idChapter)));
        } else {
            Chapter newChapter = new Chapter(formData.getChapter().getName(), formData.getChapter().getMarinesCount(), formData.getChapter().getWorld());
            chapterRepository.save(newChapter);
            spaceMarine.setChapter(newChapter);
        }

        spaceMarineRepository.save(spaceMarine);
        return true;
    }

    public boolean deleteSpaceMarine(Long id, String token) {
        Optional<SpaceMarine> optionalSpaceMarine = spaceMarineRepository.findById(id);
        if (optionalSpaceMarine.isEmpty()) {
            return false;
        }

        String userName = userService.extractUsername(token);
        SpaceMarine spaceMarine = optionalSpaceMarine.get();
//        System.out.println("Клиент: " + userName + "; права: " + userService.getUserRole(userName));
//        System.out.println("Создатель объекта: " + spaceMarine.getUserName());
//        System.out.println("-----------------");
        if (userService.getUserRole(userName).equals("user") && !spaceMarine.getUserName().equals(userName)) {
            throw new SecurityException("Нет прав на удаление этого объекта");
        }

        spaceMarineRepository.delete(spaceMarine);
        return true;
    }


    public Object getSpaceMarineById(long id) {
        return spaceMarineRepository.findById(id);
    }

    public List<SpaceMarine> getAllObjects() {
        return spaceMarineRepository.findAll();
    }

    public List<SpaceMarine> getUserObjects(String userName) {
        return spaceMarineRepository.findByUserName(userName);
    }
}
