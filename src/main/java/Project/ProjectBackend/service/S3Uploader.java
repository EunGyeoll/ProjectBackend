package Project.ProjectBackend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;

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
        amazonS3.deleteObject(bucket, fileKey);
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
