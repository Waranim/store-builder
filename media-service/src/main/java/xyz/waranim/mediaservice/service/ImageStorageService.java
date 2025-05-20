package xyz.waranim.mediaservice.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.waranim.mediaservice.config.MinioProps;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageStorageService {
    private final MinioClient minio;
    private final MinioProps props;

    public String upload(MultipartFile file) throws Exception {
        String objectName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        try (InputStream is = file.getInputStream()) {
            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        }
        return objectName;
    }

    public byte[] download(String objectName) throws Exception {
        try (GetObjectResponse res = minio.getObject(
                GetObjectArgs.builder()
                        .bucket(props.getBucket())
                        .object(objectName)
                        .build())) {
            return res.readAllBytes();
        }
    }

    public void delete(String objectName) throws Exception {
        minio.removeObject(RemoveObjectArgs.builder()
                .bucket(props.getBucket())
                .object(objectName).build());
    }
}
