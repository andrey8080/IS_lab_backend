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
import org.springframework.transaction.annotation.Transactional;
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
    private final MinioService minioService;

    public FileService(HistoryImportsRepository historyImportsRepository, SpaceMarineService spaceMarineService, UserService userService, MinioService minioService) {
        this.historyImportsRepository = historyImportsRepository;
        this.spaceMarineService = spaceMarineService;
        this.userService = userService;
        this.minioService = minioService;
    }

    public List<SpaceMarineDTO> uploadFile(MultipartFile file, String userName, LocalDateTime creationDate) {
        ObjectMapper objectMapper = new ObjectMapper();
        HistoryImports historyImport = new HistoryImports();
        historyImport.setUser(userService.getUserByUsername(userName));

        if (historyImport.getUser() == null) {
            saveFailureHistory(historyImport, "User not found");
            throw new UserNotFoundException("User not found for username: " + userName);
        }

        if (file.isEmpty()) {
            saveFailureHistory(historyImport, "Empty file uploaded");
            throw new FileParseException("The uploaded file is empty.");
        }

        List<SpaceMarineDTO> spaceMarineDTOList;
        try {
            spaceMarineDTOList = objectMapper.readValue(file.getInputStream(), new TypeReference<List<SpaceMarineDTO>>() {
            });
        } catch (IOException e) {
            saveFailureHistory(historyImport, "File parsing failed: " + e.getMessage());
            throw new FileParseException("Failed to parse the file: " + e.getMessage());
        }

        if (spaceMarineDTOList.isEmpty()) {
            saveFailureHistory(historyImport, "Parsed file is empty");
            throw new FileParseException("The file is empty or corrupted.");
        }

        Set<String> processedChapters = new HashSet<>();
        for (SpaceMarineDTO dto : spaceMarineDTOList) {
            String chapterName = dto.getChapter().getName();
            if (!processedChapters.add(chapterName)) {
                saveFailureHistory(historyImport, "failure");
                throw new ChapterAlreadyExistsException("Duplicate chapter detected: " + chapterName);
            }
        }

        spaceMarineService.addFromFile(spaceMarineDTOList, userName, creationDate);

//        String fileName = file.getOriginalFilename() + "_" +  creationDate;
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String fileUrl = saveToMinio(file, fileName);
        saveSuccessHistory(historyImport, processedChapters.size(), fileName, fileUrl);
        return spaceMarineDTOList;
    }

    @Transactional(rollbackFor = Exception.class)
    protected String saveToMinio(MultipartFile file, String fileName) {
        try {
            String fileUrl = minioService.uploadFile(fileName, file);
            return fileUrl;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла в MinIO: " + e.getMessage(), e);
        }
    }

    private void saveFailureHistory(HistoryImports historyImport, String status) {
        historyImport.setCounter(0);
        historyImport.setStatus("failure: " + status);
        historyImport.setFileName("null");
        historyImport.setFileUrl("#");
        historyImportsRepository.save(historyImport);
    }

    private void saveSuccessHistory(HistoryImports historyImport, int counter, String fileName, String fileUrl) {
        historyImport.setCounter(counter);
        historyImport.setStatus("success");
        historyImport.setFileName(fileName);
        historyImport.setFileUrl(fileUrl);
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
            historyMap.put("fileName", historyImport.getFileName());
            historyMap.put("fileUrl", historyImport.getFileUrl());
            return historyMap;
        }).collect(Collectors.toList());
    }
}