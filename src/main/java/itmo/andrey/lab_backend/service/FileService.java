package itmo.andrey.lab_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.andrey.lab_backend.domain.dto.SpaceMarineDTO;
import itmo.andrey.lab_backend.domain.entitie.HistoryImports;
import itmo.andrey.lab_backend.exception.custom.ChapterAlreadyExistsException;
import itmo.andrey.lab_backend.exception.custom.FileParseException;
import itmo.andrey.lab_backend.exception.custom.UserNotFoundException;
import itmo.andrey.lab_backend.repository.SpaceMarineRepository;
import itmo.andrey.lab_backend.service.transaction.DatabaseTransactionParticipant;
import itmo.andrey.lab_backend.service.transaction.MinioTransactionParticipant;
import itmo.andrey.lab_backend.service.transaction.TransactionCoordinator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class FileService {
    private final HistoryService historyService;
    private final SpaceMarineService spaceMarineService;
    private final UserService userService;
    private final MinioTransactionParticipant minioTransactionParticipant;
    private final TransactionCoordinator transactionCoordinator;
    private final SpaceMarineRepository spaceMarineRepository;

    public FileService(HistoryService historyService, SpaceMarineService spaceMarineService, UserService userService, MinioService minioService, MinioTransactionParticipant minioTransactionParticipant, TransactionCoordinator transactionCoordinator, SpaceMarineRepository spaceMarineRepository) {
        this.historyService = historyService;
        this.spaceMarineService = spaceMarineService;
        this.userService = userService;
        this.minioTransactionParticipant = minioTransactionParticipant;
        this.transactionCoordinator = transactionCoordinator;
        this.spaceMarineRepository = spaceMarineRepository;
    }

    public List<SpaceMarineDTO> uploadFile(MultipartFile file, String userName, LocalDateTime creationDate) {
        ObjectMapper objectMapper = new ObjectMapper();
        HistoryImports historyImport = new HistoryImports();
        var user = userService.getUserByUsername(userName);
        if (user == null) {
            historyService.saveFailureHistory(historyImport, "User not found");
            throw new UserNotFoundException("User not found for username: " + userName);
        }
        historyImport.setUser(user);

        if (historyImport.getUser() == null) {
            historyService.saveFailureHistory(historyImport, "User not found");
            throw new UserNotFoundException("User not found for username: " + userName);
        }

        if (file.isEmpty()) {
            historyService.saveFailureHistory(historyImport, "Empty file uploaded");
            throw new FileParseException("The uploaded file is empty.");
        }

        List<SpaceMarineDTO> spaceMarineDTOList;
        try {
            spaceMarineDTOList = objectMapper.readValue(file.getInputStream(), new TypeReference<List<SpaceMarineDTO>>() {
            });
        } catch (IOException e) {
            historyService.saveFailureHistory(historyImport, "File parsing failed: " + e.getMessage());
            throw new FileParseException("Failed to parse the file: " + e.getMessage());
        }

        if (spaceMarineDTOList.isEmpty()) {
            historyService.saveFailureHistory(historyImport, "Parsed file is empty");
            throw new FileParseException("The file is empty or corrupted.");
        }

        Set<String> processedChapters = new HashSet<>();
        for (SpaceMarineDTO dto : spaceMarineDTOList) {
            String chapterName = dto.getChapter().getName();
            if (!processedChapters.add(chapterName)) {
                historyService.saveFailureHistory(historyImport, "Duplicate chapter detected: " + chapterName);
                throw new ChapterAlreadyExistsException("Duplicate chapter detected: " + chapterName);
            }
        }

        DatabaseTransactionParticipant dbParticipant = new DatabaseTransactionParticipant(spaceMarineService, spaceMarineRepository, spaceMarineDTOList, userName);
        minioTransactionParticipant.setMultipartFile(file);
        minioTransactionParticipant.setFileName(UUID.randomUUID() + "_" + file.getOriginalFilename());

        transactionCoordinator.registerParticipant(dbParticipant);
        transactionCoordinator.registerParticipant(minioTransactionParticipant);

        try {
            transactionCoordinator.executeTransaction(userName);
        } catch (Exception e) {
            historyService.saveFailureHistory(historyImport, "Transaction failed: " + e.getMessage());
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        }

        return spaceMarineDTOList;
    }
}