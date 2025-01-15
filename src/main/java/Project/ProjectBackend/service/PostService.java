package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.ItemRequestDto;
import Project.ProjectBackend.dto.PostRequestDto;
import Project.ProjectBackend.dto.PostResponseDto;
import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Post;
import Project.ProjectBackend.repository.MemberRepository;
import Project.ProjectBackend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    // 1. 게시글 등록
    @Transactional
    public Post createPost(PostRequestDto postRequestDto, Member currentUser, List<MultipartFile> imageFiles) {
        // Post 엔티티 생성
        Post post = Post.builder()
                .writer(currentUser)
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .postDate(postRequestDto.getBoardDate())
                .build();

        // post 먼저 저장하여 postno 확보
        Post savedPost = postRepository.save(post);
        logger.info("Item created with ID: {}", savedPost.getPostNo());

        // 이미지 저장
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<Image> images = imageService.saveImagesForPost(imageFiles, post);
            post.setImages(images);

            // 대표 이미지 설정
            if (!images.isEmpty()) {
                post.setRepresentativeImagePath(images.get(0).getImagePath());
            }

            // 이미지 설정 후 다시 저장
            savedPost= postRepository.save(post);
        }
        return savedPost;
    }


    private String extractFileNameFromPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private String generateUniqueFileName(String path) {
        return UUID.randomUUID() + "_" + extractFileNameFromPath(path);
    }

    private long getFileSize(String path) {
        File file = new File(path);
        return file.exists() ? file.length() : 0L;
    }


    // 2. 게시글 수정
    @Transactional
    public Post updatePost(Long postId, PostRequestDto postRequestDto, List<MultipartFile> imageFiles, Member currentUser) {
        Post existingpost = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 2. 소유자 확인
        if (!existingpost.getWriter().equals(currentUser)) {
            throw new IllegalArgumentException("해당 게시글을 수정할 권한이 없습니다.");
        }

        // 3. 이미지 업데이트 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            // 기존 이미지 삭제
            imageService.deleteImages(existingpost.getImages());

            // 새로운 이미지 저장
            List<Image> updatedImages = imageService.saveImagesForPost(imageFiles, existingpost);

            // post에 새로운 이미지 설정
            existingpost.setImages(updatedImages);

            // 대표 이미지 설정
            if (!updatedImages.isEmpty()) {
                existingpost.setRepresentativeImagePath(updatedImages.get(0).getImagePath());
            }
        }
        // 새 이미지가 없는 경우 기존 이미지를 유지하므로 별도의 처리 불필요

        // 4. 나머지 속성 업데이트
        existingpost.setTitle(postRequestDto.getTitle());
        existingpost.setContent(postRequestDto.getContent());

        // 5. 게시글 저장
        return postRepository.save(existingpost);
    }


    // 3. 게시글 상세(단건) 조회
    public PostResponseDto getPost(Long postNo) {
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 조회수 증가
        post.increaseHitCount();
        postRepository.save(post);

        return PostResponseDto.from(post); // DTO 반환
    }



    // 4. 게시글 목록 조회
    public Slice<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }



    // 5. 특정 판매자가 등록한 게시글 조회
    public Slice<Post> getPostsByWriter(String memberId, Pageable pageable) {
        // 작성자 존재하는지 확인
        boolean writerExists = memberRepository.existsById(memberId);
        if (!writerExists) {
            throw new IllegalArgumentException("작성자를 찾을 수 없습니다.");
        }

        // 게시글 페이징하여 조회
        return postRepository.findByWriter_MemberId(memberId, pageable);
    }


    // 6. 게시글 삭제
    @Transactional
    public void deletePost(Long postNo) {
        if (!postRepository.existsById(postNo)) {
            throw new IllegalArgumentException("게시글이 존재하지 않습니다.");
        }
        postRepository.deleteById(postNo);
    }
}
