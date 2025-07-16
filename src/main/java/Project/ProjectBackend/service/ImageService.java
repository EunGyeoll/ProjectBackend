package Project.ProjectBackend.service;

import Project.ProjectBackend.entity.*;
import Project.ProjectBackend.exception.ImageSaveException;
import Project.ProjectBackend.repository.ImageRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository imageRepository;
    private final S3Uploader s3Uploader;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${cloud.aws.s3.folder.post}")
    private String postDir;

    @Value("${cloud.aws.s3.folder.item}")
    private String itemDir;

    @Value("${cloud.aws.s3.folder.review}")
    private String reviewDir;

    @Value("${cloud.aws.s3.folder.comment}")
    private String commentDir;

    @Value("${cloud.aws.s3.folder.profile}")
    private String profileDir;

    public List<Image> saveImagesForPost(List<MultipartFile> imageFiles, Post post) {
        return saveImagesInternal(imageFiles, postDir, post, null, null);
    }

    public List<Image> saveImagesForItem(List<MultipartFile> imageFiles, Item item) {
        return saveImagesInternal(imageFiles, itemDir, null, item, null);
    }

    public List<Image> saveImagesForReview(List<MultipartFile> imageFiles, Review review) {
        return saveImagesInternal(imageFiles, reviewDir, null, null, review);
    }

    public Image saveImageForProfile(MultipartFile imageFile, Member member) {
        validateFile(imageFile);

        if (member.getProfileImage() != null) {
            deleteImage(member.getProfileImage());
        }

        String imageUrl = s3Uploader.upload(imageFile, profileDir);

        Image image = Image.builder()
                .imagePath(imageUrl)
                .originFileName(imageFile.getOriginalFilename())
                .newFileName(getFileNameFromUrl(imageUrl))
                .fileSize(imageFile.getSize())
                .member(member)
                .build();

        return imageRepository.save(image);
    }

    public String saveCommentImage(MultipartFile imageFile, Comment comment) {
        validateFile(imageFile);
        String imageUrl = s3Uploader.upload(imageFile, commentDir);

        Image image = Image.builder()
                .imagePath(imageUrl)
                .originFileName(imageFile.getOriginalFilename())
                .newFileName(getFileNameFromUrl(imageUrl))
                .fileSize(imageFile.getSize())
                .comment(comment)
                .build();

        imageRepository.save(image);
        return imageUrl;
    }

    private List<Image> saveImagesInternal(List<MultipartFile> imageFiles, String dir, Post post, Item item, Review review) {
        List<Image> savedImages = new ArrayList<>();

        for (MultipartFile file : imageFiles) {
            validateFile(file);
            String imageUrl = s3Uploader.upload(file, dir);

            Image image = Image.builder()
                    .imagePath(imageUrl)
                    .originFileName(file.getOriginalFilename())
                    .newFileName(getFileNameFromUrl(imageUrl))
                    .fileSize(file.getSize())
                    .post(post)
                    .item(item)
                    .review(review)
                    .build();

            savedImages.add(image);
        }

        return imageRepository.saveAll(savedImages);
    }


    @Transactional
    public void deleteImage(Image image) {
        if (image != null) {
            logger.info("삭제할 이미지: {}", image.getImagePath());
            s3Uploader.delete(image.getImagePath());
            imageRepository.delete(image);
            imageRepository.flush();
            entityManager.clear();
        }
    }

    @Transactional
    public void deleteImages(List<Image> images) {
        if (images == null || images.isEmpty()) return;

        for (Image image : images) {
            try {
                deleteImage(image);
            } catch (Exception e) {
                logger.warn("이미지 삭제 중 오류 발생: {}", image.getImagePath(), e);
            }
        }

        imageRepository.flush();
        entityManager.clear();
    }

    @Transactional
    public void deleteImageByPath(String imagePath) {
        if (imagePath == null) return;

        Image image = imageRepository.findByImagePath(imagePath)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다: " + imagePath));

        deleteImage(image);
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFileName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFileName).toLowerCase();
        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp");

        if (contentType == null || !contentType.startsWith("image/")) {
            if (!allowedExtensions.contains(extension)) {
                throw new ImageSaveException("허용되지 않는 파일 형식입니다: " + originalFileName);
            }
        }
    }



    // HTML 내용에서 이미지 URL 추출
    public List<String> extractImageUrlsFromContent(String html) {
        List<String> imageUrls = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        for (Element img : doc.select("img")) {
            imageUrls.add(img.attr("src"));
        }
        return imageUrls;
    }

    // URL 리스트를 기반으로 Image 엔티티 저장
    public List<Image> registerImagesFromContent(List<String> imageUrls, Post post) {
        List<Image> images = new ArrayList<>();

        for (String url : imageUrls) {
            Image image = Image.builder()
                    .imagePath(url)
                    .originFileName(getFileNameFromUrl(url))
                    .newFileName(getFileNameFromUrl(url))
                    .fileSize(0L)
                    .post(post)
                    .build();
            images.add(image);
        }

        return imageRepository.saveAll(images);
    }

    // URL에서 파일명 추출
    public String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}





