package itmo.andrey.lab_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.andrey.lab_backend.domain.dto.SpaceMarineDTO;
import itmo.andrey.lab_backend.domain.entitie.HistoryImports;
import itmo.andrey.lab_backend.exception.custom.ChapterAlreadyExistsException;
import itmo.andrey.lab_backend.exception.custom.FileParseException;
import itmo.andrey.lab_backend.exception.custom.UserNotFoundException;
import itmo.andrey.lab_backend.repository.HistoryImportsRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FileService {
    private final HistoryImportsRepository historyImportsRepository;
    private final SpaceMarineService spaceMarineService;
    private final UserService userService;

    public FileService(HistoryImportsRepository historyImportsRepository, SpaceMarineService spaceMarineService, UserService userService) {
        this.historyImportsRepository = historyImportsRepository;
        this.spaceMarineService = spaceMarineService;
        this.userService = userService;
    }

    public List<SpaceMarineDTO> uploadFile(MultipartFile file, String userName, LocalDateTime creationDate) {
        ObjectMapper objectMapper = new ObjectMapper();
        HistoryImports historyImport = new HistoryImports();
        historyImport.setUser(userService.getUserByUsername(userName));

        // Проверяем существование пользователя
        if (historyImport.getUser() == null) {
            saveFailureHistory(historyImport, "User not found");
            throw new UserNotFoundException("User not found for username: " + userName);
        }

        // Проверяем, что файл не пустой
        if (file.isEmpty()) {
            saveFailureHistory(historyImport, "Empty file uploaded");
            throw new FileParseException("The uploaded file is empty.");
        }

        List<SpaceMarineDTO> spaceMarineDTOList;
        try {
            // Считываем данные из файла
            spaceMarineDTOList = objectMapper.readValue(file.getInputStream(), new TypeReference<List<SpaceMarineDTO>>() {});
        } catch (IOException e) {
            saveFailureHistory(historyImport, "File parsing failed: " + e.getMessage());
            throw new FileParseException("Failed to parse the file: " + e.getMessage());
        }

        // Проверяем, что список не пустой
        if (spaceMarineDTOList.isEmpty()) {
            saveFailureHistory(historyImport, "Parsed file is empty");
            throw new FileParseException("The file is empty or corrupted.");
        }

        // Проверяем на дублирование Chapter name
        Set<String> processedChapters = new HashSet<>();
        for (SpaceMarineDTO dto : spaceMarineDTOList) {
            String chapterName = dto.getChapter().getName();
            if (!processedChapters.add(chapterName)) {
                saveFailureHistory(historyImport, "failure");
                throw new ChapterAlreadyExistsException("Duplicate chapter detected: " + chapterName);
            }
        }

        // Добавляем данные через сервис
        spaceMarineService.addFromFile(spaceMarineDTOList, userName, creationDate);

        // Сохраняем успешную историю импорта
        saveSuccessHistory(historyImport, processedChapters.size());
        return spaceMarineDTOList;
    }

    private void saveFailureHistory(HistoryImports historyImport, String status) {
        historyImport.setCounter(0);
        historyImport.setStatus("failure: " + status);
        historyImportsRepository.save(historyImport);
    }

    private void saveSuccessHistory(HistoryImports historyImport, int counter) {
        historyImport.setCounter(counter);
        historyImport.setStatus("success");
        historyImportsRepository.save(historyImport);
    }

    private List<HistoryImports> getHistoryFromDB(String userRole, String userName) {
        return "admin".equals(userRole)
                ? historyImportsRepository.findAll()
                : historyImportsRepository.getAllByUser_Name(userName);
    }

    public List<Map<String, Object>> getHistory(String token) {
        String userRole = userService.getUserRole(token);
        String username = userService.extractUsername(token);
        List<HistoryImports> historyImportsList = getHistoryFromDB(userRole, username);

        return historyImportsList.stream().map(historyImport -> {
            Map<String, Object> historyMap = new HashMap<>();
            historyMap.put("id", historyImport.getId());
            historyMap.put("status", historyImport.getStatus());
            historyMap.put("counter", historyImport.getCounter());
            historyMap.put("username", historyImport.getUser().getName());
            return historyMap;
        }).collect(Collectors.toList());
    }
}