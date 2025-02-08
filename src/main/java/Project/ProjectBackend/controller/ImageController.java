package Project.ProjectBackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) throws IOException {
        // 파일 이름만 전달받으므로 uploadDir과 결합
        File file = Paths.get(uploadDir, fileName).toFile();

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(file.toPath()));
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        Resource resource = new UrlResource(file.toURI());

        return ResponseEntity.ok().headers(headers).body(resource);
    }

}
