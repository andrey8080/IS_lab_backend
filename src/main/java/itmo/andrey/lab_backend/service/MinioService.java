package itmo.andrey.lab_backend.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MinioService {
    private final MinioClient minioClient;
    private final String minioUrl;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinioService(MinioClient minioClient, @Value("${minio.url}") String minioUrl) {
        this.minioClient = minioClient;
        this.minioUrl = minioUrl;
    }

    public String uploadFile(String fileName, MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return minioUrl + "/" + bucketName + "/" + fileName;
        } catch (UnexpectedRollbackException ex) {
                throw new UnexpectedRollbackException("Ошибка загрузки файла в MinIO: " + ex.getMessage(), ex);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла в MinIO: " + e.getMessage(), e);
        }
    }

    public boolean isMinioAvailable() {
        try {
            minioClient.listBuckets();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка удаления файла из MinIO", e);
        }
    }
}