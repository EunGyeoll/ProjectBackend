    package Project.ProjectBackend.service;

    import Project.ProjectBackend.entity.Image;
    import Project.ProjectBackend.entity.Item;
    import Project.ProjectBackend.entity.Post;
    import Project.ProjectBackend.exception.ImageSaveException;
    import Project.ProjectBackend.repository.ImageRepository;
    import lombok.RequiredArgsConstructor;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.File;
    import java.io.IOException;
    import java.util.List;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    public class ImageService {

        private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

        private final ImageRepository imageRepository;

        @Value("${file.upload-dir}") // application.properties에 업로드 디렉토리 설정
        private String uploadDir;

        /**
         * 이미지 저장 로직
         */

        // POST에서의 이미지 저장
        public List<Image> saveImagesForPost(List<MultipartFile> imageFiles, Post post) {
            return imageFiles.stream().map(file -> {
                String originalFileName = file.getOriginalFilename();
                String uniqueFileName = generateUniqueFileName(originalFileName);
                String filePath = uploadDir + File.separator + uniqueFileName;

//                try {
//                    // 파일 저장
//                    File dest = new File(filePath);
//                    file.transferTo(dest);
//                } catch (IOException e) {
//                    throw new ImageSaveException("이미지 저장에 실패했습니다: " + originalFileName, e);
//                }
                try {
                    // 파일 저장 디렉토리 생성 (존재하지 않을 경우)
                    File dir = new File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    // 파일 저장
                    File dest = new File(filePath);
                    file.transferTo(dest);
                } catch (IOException e) {
                    logger.error("Failed to save image: {}", originalFileName, e);
                    throw new ImageSaveException("이미지 저장에 실패했습니다: " + originalFileName, e);
                }

                // Image 엔티티 생성
                Image image = Image.builder()
                        .imagePath(filePath)
                        .originFileName(originalFileName)
                        .newFileName(uniqueFileName)
                        .fileSize(file.getSize())
                        .post(post)
                        .build();

                return imageRepository.save(image);
            }).collect(Collectors.toList());
        }


        // ITEM에서의 이미지 저장
        public List<Image> saveImagesForItem(List<MultipartFile> imageFiles, Item item) {
            return imageFiles.stream().map(file -> {
                String originalFileName = file.getOriginalFilename();
                String uniqueFileName = generateUniqueFileName(originalFileName);
                String filePath = uploadDir + File.separator + uniqueFileName;

//                try {
//                    // 파일 저장
//                    File dest = new File(filePath);
//                    file.transferTo(dest);
//                } catch (IOException e) {
//                    throw new ImageSaveException("이미지 저장에 실패했습니다: " + originalFileName, e);
//                }

                try {
                    // 파일 저장 디렉토리 생성 (존재하지 않을 경우)
                    File dir = new File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    // 파일 저장
                    File dest = new File(filePath);
                    file.transferTo(dest);
                } catch (IOException e) {
                    logger.error("Failed to save image: {}", originalFileName, e);
                    throw new ImageSaveException("이미지 저장에 실패했습니다: " + originalFileName, e);
                }

                // Image 엔티티 생성
                Image image = Image.builder()
                        .imagePath(filePath)
                        .originFileName(originalFileName)
                        .newFileName(uniqueFileName)
                        .fileSize(file.getSize())
                        .item(item)
                        .build();

                return imageRepository.save(image);
            }).collect(Collectors.toList());
        }




        /**
         * 기존 이미지 삭제 로직 (필요 시)
         */
        public void deleteImages(List<Image> images) {
            for (Image image : images) {
                File file = new File(image.getImagePath());
                if (file.exists()) {
                    file.delete();
                }
                imageRepository.delete(image);
            }
        }

        /**
         * 유니크한 파일명 생성
         */
        private String generateUniqueFileName(String originalFileName) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            return timestamp + "_" + originalFileName;
        }
    }
