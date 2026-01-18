package vnikolaenko.github.jarch.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    // Создать бакет если не существует
    @PostConstruct
    public void init() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }
    }

    // 📤 Сохранить файл
    public String uploadFile(Path filePath, String filename) throws Exception {
        if (filename == null || filename.isEmpty()) {
            filename = UUID.randomUUID() + "_" + filePath.getFileName().toString();
        }

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .stream(Files.newInputStream(filePath), Files.size(filePath), -1)
                        .contentType(Files.probeContentType(filePath))
                        .build()
        );

        log.info("Файл {} сохранен в MinIO из пути {}", filename, filePath);
        return filename;
    }

    // 📤 Сохранить файл из byte[]
    public String uploadFile(byte[] fileBytes, String filename) throws Exception {
        if (filename == null || filename.isEmpty()) {
            filename = UUID.randomUUID().toString();
        }

        String contentType = "application/json";

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(inputStream, fileBytes.length, -1)
                            .contentType(contentType)
                            .build()
            );
        }

        log.info("Файл {} сохранен в MinIO из byte[], размер: {} bytes", filename, fileBytes.length);
        return filename;
    }

    // 📥 Скачать файл
    public byte[] downloadFile(String filename) throws Exception {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build()
        )) {
            return stream.readAllBytes();
        }
    }

    // 📄 Получить информацию о файле
    public StatObjectResponse getFileInfo(String filename) throws Exception {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build()
        );
    }

    // 🗑️ Удалить файл
    public void deleteFile(String filename) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build()
        );
        log.info("Файл {} удален из MinIO", filename);
    }

    // 📋 Список файлов
    public List<String> listFiles() throws Exception {
        List<String> files = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );

        for (Result<Item> result : results) {
            files.add(result.get().objectName());
        }
        return files;
    }

    // 🔗 Получить временную ссылку для скачивания
    public String getPresignedUrl(String filename) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(filename)
                        .expiry(60 * 60) // 1 час
                        .build()
        );
    }
}
