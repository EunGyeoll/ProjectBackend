package Project.ProjectBackend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;
    private final Logger logger = LoggerFactory.getLogger(S3Uploader.class);

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String dirName) {
        validateImageFile(file); // 이미지 파일인지 검증

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String fileNameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String fileName = dirName + "/" + fileNameWithoutExt + "_" + UUID.randomUUID() + fileExtension;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);

            return amazonS3.getUrl(bucket, fileName).toString();
        } catch (IOException e) {
            throw new IllegalStateException("S3 업로드 실패: " + file.getOriginalFilename());
        }
    }


    public void delete(String fileUrl) {
        String fileKey = fileUrl.substring(fileUrl.indexOf(".com/") + 5);
        String decodedKey = URLDecoder.decode(fileKey, StandardCharsets.UTF_8);

//        logger.info("S3 삭제 대상 key: {}", fileKey);  // 이 값이 실제 S3의 키와 일치해야 함
        logger.info("디코딩된 S3 삭제 대상 key: {}", decodedKey);

        amazonS3.deleteObject(bucket, decodedKey);
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFileName = file.getOriginalFilename();

        // 확장자로 파일 유형 체크
        String extension = originalFileName != null
                ? originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase()
                : "";

        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp");

        if (contentType == null || !contentType.startsWith("image/") || !allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 이미지 형식입니다.");
        }
    }
}
