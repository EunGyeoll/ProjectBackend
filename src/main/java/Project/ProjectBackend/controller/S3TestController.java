package Project.ProjectBackend.controller;

import Project.ProjectBackend.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class S3TestController {

    private final S3Uploader s3Uploader;

    @PostMapping("/test-upload")
    public ResponseEntity<String> upload(@RequestPart MultipartFile file) {
        String url = s3Uploader.upload(file, "test");
        return ResponseEntity.ok(url);
    }
}
