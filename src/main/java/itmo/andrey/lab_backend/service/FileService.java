// FileService.java
package itmo.andrey.lab_backend.service;

import itmo.andrey.lab_backend.domain.entitie.HistoryImports;
import itmo.andrey.lab_backend.repository.HistoryImportsRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileService {
    private final HistoryImportsRepository historyImportsRepository;

    public FileService(HistoryImportsRepository historyImportsRepository) {
        this.historyImportsRepository = historyImportsRepository;
    }

    public void uploadFile() {
    }

    public List<HistoryImports> getHistory(String userRole, String userName) {
        if (userRole.equals("admin")) {
            return historyImportsRepository.findAll();
        } else {
            return historyImportsRepository.getAllByUser_Name(userName);
        }
    }
}