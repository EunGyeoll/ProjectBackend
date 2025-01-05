package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.PostRequestDto;
import Project.ProjectBackend.dto.PostResponseDto;
import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Post;
import Project.ProjectBackend.repository.MemberRepository;
import Project.ProjectBackend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
    private final FileService fileService;

    // 게시글 등록
    public Post createPost(PostRequestDto postRequestDto) {
        Member writer = memberRepository.findById(postRequestDto.getWriterId())
                .orElseThrow(() -> new IllegalArgumentException("작성자가 존재하지 않습니다."));

        // Post 엔티티 생성
        Post post = Post.builder()
                .writer(writer)
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .postDate(postRequestDto.getBoardDate())
                .build();

        // 이미지 설정
        List<Image> images = postRequestDto.getImagePaths().stream()
                .map(path -> Image.builder()
                        .imagePath(path)
                        .originFileName(extractFileNameFromPath(path))
                        .newFileName(generateUniqueFileName(path))
                        .fileSize(getFileSize(path))
                        .post(post)
                        .build())
                .collect(Collectors.toList());
        post.setImages(images);

        // 대표 이미지 설정
        if (!images.isEmpty()) {
            post.setRepresentativeImagePath(images.get(0).getImagePath());
        }

        return postRepository.save(post);
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

    // 게시글 수정
    public PostResponseDto updatePost(Long postNo, PostRequestDto postRequestDto) {
        Post existingPost = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 이미지 업데이트 처리
        if (postRequestDto.getImagePaths() != null && !postRequestDto.getImagePaths().isEmpty()) {
            List<Image> updatedImages = postRequestDto.getImagePaths().stream()
                    .map(path -> Image.builder()
                            .imagePath(path)
                            .originFileName(extractFileNameFromPath(path))
                            .newFileName(generateUniqueFileName(path))
                            .fileSize(getFileSize(path))
                            .post(existingPost) // 연관관계 설정
                            .build())
                    .collect(Collectors.toList());
            existingPost.setImages(updatedImages);

            // 대표 이미지 설정
            existingPost.setRepresentativeImagePath(updatedImages.get(0).getImagePath());
        }

        // 나머지 속성 업데이트
        existingPost.setTitle(postRequestDto.getTitle());
        existingPost.setContent(postRequestDto.getContent());

        postRepository.save(existingPost);
        return PostResponseDto.from(existingPost);
    }

    public List<Image> getExistingImages(Long postNo) {
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));
        return post.getImages();
    }

    // 모든 게시글 조회
    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponseDto::from) // 엔티티 -> DTO 변환
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    public PostResponseDto getPost(Long postNo) {
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 조회수 증가
        post.increaseHitCount();
        postRepository.save(post);

        return PostResponseDto.from(post); // DTO 반환
    }

    // 게시글 삭제
    public void deletePost(Long postNo) {
        if (!postRepository.existsById(postNo)) {
            throw new IllegalArgumentException("게시글이 존재하지 않습니다.");
        }
        postRepository.deleteById(postNo);
    }
}
