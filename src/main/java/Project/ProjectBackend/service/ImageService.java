    package Project.ProjectBackend.service;

    import Project.ProjectBackend.entity.Image;
    import Project.ProjectBackend.entity.Item;
    import Project.ProjectBackend.entity.Post;
    import Project.ProjectBackend.exception.ImageSaveException;
    import Project.ProjectBackend.repository.ImageRepository;
    import lombok.RequiredArgsConstructor;
    import org.apache.commons.io.FilenameUtils;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.File;
    import java.io.IOException;
    import java.nio.file.Paths;
    import java.util.ArrayList;
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

        // POST 관련된 이미지를 저장하는 메소드
        public List<Image> saveImagesForPost(List<MultipartFile> imageFiles, Post post) {
            return saveImagesInternal(imageFiles, post, null);
        }



        // ITEM 관련된 이미지를 저장하는 메소드
        public List<Image> saveImagesForItem(List<MultipartFile> imageFiles, Item item) {
            return saveImagesInternal(imageFiles, null, item);
        }


        // 공통적인 이미지 저장 로직
        private List<Image> saveImagesInternal(List<MultipartFile> imageFiles, Post post, Item item) {
            List<Image> savedImages = new ArrayList<>();
            int existingImageCount = (item != null) ? item.getImages().size() : (post != null ? post.getImages().size() : 0);

            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);
                validateFile(file);

                String originalFileName = file.getOriginalFilename();
                if (originalFileName == null) {
                    throw new ImageSaveException("파일명이 없습니다.");
                }

                String baseName = FilenameUtils.getBaseName(originalFileName);
                String extension = FilenameUtils.getExtension(originalFileName);
                String uniqueFileName = baseName + "." + extension; // 기본 파일명

                // 파일 경로
                String filePath = Paths.get(uploadDir, uniqueFileName).toString().replace("\\", "/");

                // 중복된 파일명 처리
                int duplicateCount = 1;
                while (new File(filePath).exists()) {
                    uniqueFileName = baseName + "_" + duplicateCount + "." + extension;
                    filePath = Paths.get(uploadDir, uniqueFileName).toString().replace("\\", "/");
                    duplicateCount++;
                }

                try {
                    // 파일 저장 디렉토리 생성 (존재하지 않을 경우)
                    File dir = new File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    // 파일 저장
                    File dest = new File(filePath);
                    file.transferTo(dest);
                    logger.info("Saved image file: {}", filePath);
                } catch (IOException e) {
                    throw new ImageSaveException("이미지 저장에 실패했습니다: " + originalFileName, e);
                }

                // Image 엔티티 생성
                Image image = Image.builder()
                        .imagePath(filePath)
                        .originFileName(originalFileName)
                        .newFileName(uniqueFileName)
                        .fileSize(file.getSize())
                        .post(post)
                        .item(item)
                        .build();

                savedImages.add(imageRepository.save(image));
            }

            return savedImages;
        }



        public void deleteImages(List<Image> images) {
            for (Image image : images) {
                File file = new File(image.getImagePath());
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        throw new ImageSaveException("이미지를 삭제하는데 실패했습니다: " + image.getImagePath());
                    }
                }
                imageRepository.delete(image);
            }
        }


        // 파일이 이미지인지 검증
        private void validateFile(MultipartFile file) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ImageSaveException("허용되지 않는 파일 형식입니다: " + file.getOriginalFilename());
            }
        }

    }
