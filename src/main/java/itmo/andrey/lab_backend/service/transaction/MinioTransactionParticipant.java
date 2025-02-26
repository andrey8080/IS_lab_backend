package itmo.andrey.lab_backend.service.transaction;

import itmo.andrey.lab_backend.domain.entitie.HistoryImports;
import itmo.andrey.lab_backend.domain.entitie.User;
import itmo.andrey.lab_backend.exception.custom.MinioConnectionException;
import itmo.andrey.lab_backend.service.MinioService;
import itmo.andrey.lab_backend.service.HistoryService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MinioTransactionParticipant implements TransactionParticipant {

    private final MinioService minioService;
    private final HistoryService historyService;
    @Getter @Setter
    private String fileName;
    @Setter
    private MultipartFile multipartFile;
    private boolean prepared = false;

    public MinioTransactionParticipant(MinioService minioService, HistoryService historyService) {
        this.minioService = minioService;
        this.historyService = historyService;
    }

    @Override
    public boolean prepare() {
        if (minioService.isMinioAvailable()) {
            System.out.println("MinIO: Успешно подготовлено");
            prepared = true;
            return true;
        } else {
            System.out.println("MinIO: Подготовка не удалась");
            throw new RuntimeException("Проблемы с MiniO");
        }
    }

    @Override
    public String commit(User user) {
        if (prepared) {
            try {
                String fileUrl = minioService.uploadFile(fileName, multipartFile);
                HistoryImports historyImport = new HistoryImports();
                historyImport.setUser(user);
                historyService.saveSuccessHistory(historyImport, 1, fileName, fileUrl);
                return "MinIO commit success";
            } catch (Exception e) {
                throw new MinioConnectionException("Ошибка загрузки файла в MinIO: " + e.getMessage(), e);
            }
        }
        return "MinIO commit failed";
    }

    @Override
    public void rollback() {
        if (prepared) {
            minioService.deleteFile(fileName);
            System.out.println("MinIO: Changes rolled back");
        }
    }
}