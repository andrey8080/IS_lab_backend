package itmo.andrey.lab_backend.service;

import itmo.andrey.lab_backend.domain.entitie.HistoryImports;
import itmo.andrey.lab_backend.repository.HistoryImportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HistoryService {
    private final UserService userService;
    private final HistoryImportsRepository historyImportsRepository;
    HistoryImports historyImport;

    @Autowired
    public HistoryService(UserService userService, HistoryImportsRepository historyImportsRepository) {
        this.userService = userService;
        this.historyImportsRepository = historyImportsRepository;
    }

    public void saveFailureHistory(HistoryImports historyImport, String status) {
        historyImport.setCounter(0);
        historyImport.setStatus("failure: " + status);
        historyImport.setFileName("null");
        historyImport.setFileUrl("#");
        historyImportsRepository.save(historyImport);
    }

    public void saveSuccessHistory(HistoryImports historyImport, int counter, String fileName, String fileUrl) {
        historyImport.setCounter(counter);
        historyImport.setStatus("success");
        historyImport.setFileName(fileName);
        historyImport.setFileUrl(fileUrl);
        historyImportsRepository.save(historyImport);
    }

    private List<HistoryImports> getHistoryFromDB(String userName) {
        return userService.getUserRole(userName).equals("admin")
                ? historyImportsRepository.findAll()
                : historyImportsRepository.getAllByUser_Name(userName);
    }

    public List<Map<String, Object>> getHistory(String token) {
        String username = userService.extractUsername(token);
        List<HistoryImports> historyImportsList = getHistoryFromDB(username);

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
