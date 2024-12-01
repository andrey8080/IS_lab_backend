package itmo.andrey.lab1_backend.controller;

import itmo.andrey.lab1_backend.domain.entitie.Chapter;
import itmo.andrey.lab1_backend.domain.entitie.SpaceMarine;
import itmo.andrey.lab1_backend.domain.dto.ChapterDTO;
import itmo.andrey.lab1_backend.repository.ChapterRepository;
import itmo.andrey.lab1_backend.repository.SpaceMarineRepository;
import itmo.andrey.lab1_backend.service.UserService;
import itmo.andrey.lab1_backend.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
@RequestMapping("/chapter")
public class ChapterController {
    private final ChapterRepository chapterRepository;
    private final SpaceMarineRepository spaceMarineRepository;
    private final UserService userService;

    @Autowired
    public ChapterController(UserService userService, ChapterRepository chapterRepository, SpaceMarineRepository spaceMarineRepository) {
        this.chapterRepository = chapterRepository;
        this.spaceMarineRepository = spaceMarineRepository;
        this.userService = userService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody ChapterDTO formData, @RequestHeader("Authorization") String token) {
        boolean validToken = userService.checkValidToken(token);

        if (validToken) {
            Chapter chapter = new Chapter();
            try {
                chapter.setName(formData.getName());
                chapter.setCount(formData.getMarinesCount());
                chapter.setWorld(formData.getWorld());
                chapterRepository.save(chapter);
                return ResponseEntity.ok("{\"msg\":\"Chapter успешно добавлен\"}");
            } catch (DataIntegrityViolationException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\":\"Орден с таким именем уже существует. Пожалуйста, выберите другое имя.\"}");
            } catch (Exception e) {
                return ResponseEntity.status(400).body("{\"error\":\"Произошла ошибка добавления Chapter: " + e.getMessage() + "\"}");
            }
        } else {
            return ResponseEntity.status(400).body("{\"error\":\"Ошибка обработки токена\"}");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteChapterWithReassignment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "newChapterId", required = false) Long newChapterId,
            @RequestParam(value = "deleteSpaceMarines", defaultValue = "false") boolean deleteSpaceMarines) {

        boolean validToken = userService.checkValidToken(token);

        if (validToken) {
            return chapterRepository.findById(id).map(chapter -> {
                List<SpaceMarine> marines = spaceMarineRepository.findByChapter(chapter);

                if (newChapterId != null) {
                    Chapter newChapter = chapterRepository.findById(newChapterId).orElse(null);
                    if (newChapter == null) {
                        return ResponseEntity.status(400).body("{\"error\":\"Новый орден не найден\"}");
                    }

                    for (SpaceMarine marine : marines) {
                        marine.setChapter(newChapter);
                        spaceMarineRepository.save(marine);
                    }

                    chapterRepository.delete(chapter);
                    return ResponseEntity.ok().body("{\"message\":\"Chapter успешно удален и космодесантники переназначены\"}");
                } else if (deleteSpaceMarines) {
                    spaceMarineRepository.deleteAll(marines);
                    chapterRepository.delete(chapter);
                    return ResponseEntity.ok().body("{\"message\":\"Chapter и связанные SpaceMarine успешно удалены\"}");
                } else {
                    return ResponseEntity.status(400).body("{\"error\":\"Не указано действие для связанных SpaceMarine (удаление или переназначение)\"}");
                }
            }).orElseGet(() -> ResponseEntity.status(404).body("{\"error\":\"Chapter не найден\"}"));

        } else {
            return ResponseEntity.status(401).body("{\"error\":\"Неверный или просроченный токен\"}");
        }
    }

    @PostMapping("/getAll")
    public ResponseEntity<?> getAllChapters() {
        List<Chapter> allChapters = chapterRepository.findAll();
        return ResponseEntity.ok(allChapters);
    }
}
