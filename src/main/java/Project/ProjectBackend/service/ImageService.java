package Project.ProjectBackend.service;

import Project.ProjectBackend.entity.*;
import Project.ProjectBackend.exception.ImageSaveException;
import Project.ProjectBackend.repository.ImageRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository imageRepository;

    @PersistenceContext
    private EntityManager entityManager; // EntityManager 주입

    @Value("${file.upload-dir}") // application.properties에 업로드 디렉토리 설정
    private String uploadDir;

    /**
     * 이미지 저장 로직
     */

    // POST 관련 이미지를 저장
    public List<Image> saveImagesForPost(List<MultipartFile> imageFiles, Post post) {
        return saveImagesInternal(imageFiles, post, null, null );
    }

    // ITEM 관련 이미지를 저장
    public List<Image> saveImagesForItem(List<MultipartFile> imageFiles, Item item) {
        return saveImagesInternal(imageFiles, null, item, null );
    }

    // REVIEW 관련 이미지를 저장
    public List<Image> saveImagesForReview(List<MultipartFile> imageFiles, Review review) {
        return saveImagesInternal(imageFiles, null, null, review);
    }

    // 프로필 이미지를 저장
    public Image saveImageForProfile(MultipartFile imageFile, Member member) {
        validateFile(imageFile);

        String savedFilePath = saveSingleFile(imageFile);

        // 기존 프로필 이미지 삭제
        if (member.getProfileImage() != null) {
            deleteImage(member.getProfileImage());
        }

        // 새로운 프로필 이미지 저장
        Image image = Image.builder()
                .imagePath(savedFilePath)
                .originFileName(imageFile.getOriginalFilename())
                .newFileName(new File(savedFilePath).getName())
                .fileSize(imageFile.getSize())
                .member(member) // Member와 연관 설정
                .build();

        return imageRepository.save(image);
    }

    // 다중 이미지를 저장하는 공통 메서드 (게시글, 아이템, 리뷰에 사용)
    private List<Image> saveImagesInternal(List<MultipartFile> imageFiles, Post post, Item item, Review review) {
        List<Image> savedImages = new ArrayList<>();
        List<File> createdFiles = new ArrayList<>();

        try {
            for (MultipartFile file : imageFiles) {
                validateFile(file);

                // 단일 파일 저장
                String savedFilePath = saveSingleFile(file);
                createdFiles.add(new File(savedFilePath));

                // Image 엔티티 생성
                Image image = Image.builder()
                        .imagePath(savedFilePath)
                        .originFileName(file.getOriginalFilename())
                        .newFileName(new File(savedFilePath).getName())
                        .fileSize(file.getSize())
                        .post(post)
                        .item(item)
                        .review(review)
                        .build();

                savedImages.add(image);
            }

            // JPA 배치 저장 (리스트로 한 번에 저장)
            imageRepository.saveAll(savedImages);

            return savedImages;
        } catch (Exception e) {
            // 저장 실패 시 롤백: 생성된 파일 삭제
            for (File createdFile : createdFiles) {
                if (createdFile.exists()) {
                    createdFile.delete();
                }
            }
            throw new ImageSaveException("다중 이미지 저장에 실패했습니다.", e);
        }
    }

    // 단일 파일 저장 로직
    private String saveSingleFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new ImageSaveException("파일명이 없습니다.");
        }

        // 확장자 추출
        String extension = FilenameUtils.getExtension(originalFileName);
        String baseName = FilenameUtils.getBaseName(originalFileName);

        // 파일명이 너무 길어지지 않도록 원본 파일명 일부만 사용 (최대 20자)
        String trimmedFileName = baseName.length() > 20 ? baseName.substring(0, 20) : baseName;

        // 파일명에서 불필요한 특수 문자 제거 (영어, 숫자, 한글, `_`, `-`, `()`, `+`, `.` 허용)
        String safeFileName = trimmedFileName.replaceAll("[^a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ()_+.-]", "");

        // UUID + 안전한 파일명 조합하여 새로운 파일명 생성
        String uniqueFileName = UUID.randomUUID().toString() + "_" + safeFileName + "." + extension;

        // 저장할 파일 경로 설정
        String filePath = Paths.get(uploadDir, uniqueFileName).toString().replace("\\", "/");

        try {
            // 디렉토리 없으면 생성
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new ImageSaveException("업로드 디렉토리 생성 실패: " + uploadDir);
            }

            // 파일 저장
            File dest = new File(filePath);
            file.transferTo(dest);
            logger.info("Saved image file: {}", filePath);
        } catch (IOException e) {
            throw new ImageSaveException("이미지 저장에 실패했습니다: " + originalFileName, e);
        }

        return filePath;
    }



    // 이미지 삭제 로직
    @Transactional
    public void deleteImages(List<Image> images) {
        for (Image image : images) {
            deleteImage(image);
        }
    }

    @Transactional
    public void deleteImage(Image image) {
        if (image != null) {
            logger.info("🔹 삭제할 이미지: {}", image.getImagePath());

            // 데이터베이스에서 삭제 전 확인
            boolean existsBeforeDelete = imageRepository.existsById(image.getImageId());
            logger.info("🔍 삭제 전 DB 존재 여부: {}", existsBeforeDelete);

            // 파일 시스템에서 삭제
            File file = new File(image.getImagePath());
            if (file.exists() && !file.delete()) {
                throw new IllegalStateException("이미지를 삭제할 수 없습니다: " + image.getImagePath());
            }

            // 데이터베이스에서 삭제
            imageRepository.delete(image);
            imageRepository.flush();
            entityManager.clear(); // 영속성 컨텍스트 초기화

            // 삭제 후 확인
            boolean existsAfterDelete = imageRepository.existsById(image.getImageId());
            logger.info("✅ 삭제 후 DB 존재 여부: {}", existsAfterDelete);
        }
    }





    // 파일이 이미지인지 검증
    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFileName = file.getOriginalFilename();

        // 확장자로 파일 유형 체크
        String extension = FilenameUtils.getExtension(originalFileName).toLowerCase();
        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp");

        if (contentType == null || !contentType.startsWith("image/")) {
            if (!allowedExtensions.contains(extension)) {
                throw new ImageSaveException("허용되지 않는 파일 형식입니다: " + originalFileName);
            }
        }
    }

}
