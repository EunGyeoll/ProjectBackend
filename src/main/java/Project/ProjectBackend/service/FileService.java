    package Project.ProjectBackend.service;

    import Project.ProjectBackend.entity.Image;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.File;
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.util.UUID;

    @Service
    public class FileService {

        @Value("${file.upload-dir}") // application.yml의 경로 사용
        private String uploadDir;

    //    @Value("${file.base-url}") // base URL 사용
    //    private String baseUrl;

        public Image uploadImage(MultipartFile file) {
            // 이미지를 저장하면서 DTO 생성
            String uniqueFilename = saveFile(file);
            return Image.builder()
                    .originFileName(file.getOriginalFilename())
                    .imagePath(uniqueFilename)
                    .fileSize(file.getSize())
                    .build();
        }


        public String saveFile(MultipartFile file) {
            try {
                // 원본 파일 이름 가져오기
                String originalFilename = file.getOriginalFilename();

                // 저장 경로 설정
                File destinationFile = new File(uploadDir, originalFilename);

                // 같은 이름의 파일이 존재할 경우 충돌 방지
                int count = 1;
                while (destinationFile.exists()) {
                    String fileNameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
                    String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                    destinationFile = new File(uploadDir, fileNameWithoutExt + "_" + count + fileExtension);
                    count++;
                }

                // 4. 파일 저장
                file.transferTo(destinationFile);

                // 5. 저장된 파일 경로 반환
                return destinationFile.getAbsolutePath();
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
            }
        }


        // 파일 이름을 임의로 바꾸어 저장하는 메소드
    //    public String saveFile(MultipartFile file) {
    //        if (file.isEmpty()) {
    //            throw new IllegalArgumentException("파일이 비어 있습니다.");
    //        }
    //
    //        try {
    //            // 고유 파일명 생성
    //            String originalFilename = file.getOriginalFilename();
    //            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    //            String uniqueFilename = UUID.randomUUID().toString() + extension;
    //
    //            // 디렉토리 확인 및 생성
    //            Path uploadPath = Paths.get(uploadDir);
    //            if (!Files.exists(uploadPath)) {
    //                Files.createDirectories(uploadPath); // 디렉토리가 없으면 생성
    //            }
    //
    //            // 파일 저장 경로
    //            Path filePath = uploadPath.resolve(uniqueFilename);
    //
    //            // 파일 저장
    //            file.transferTo(filePath.toFile());
    //
    //            // 반환 경로 설정 (클라이언트가 접근 가능한 경로로 변환 필요)
    //            return filePath.toString();
    //        } catch (IOException e) {
    //            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
    //        }
    //    }



        public boolean deleteFile(String filename) {
            Path filePath = Paths.get(uploadDir, filename);
            try {
                return Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new RuntimeException("파일 삭제 중 오류 발생", e);
            }
        }

        private String getExtension(String filename) {
            return filename.substring(filename.lastIndexOf("."));
        }
    }
