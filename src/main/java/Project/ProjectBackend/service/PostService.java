package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.*;
import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Post;
import Project.ProjectBackend.entity.PostCategory;
import Project.ProjectBackend.repository.*;
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

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;
    private final PostCategoryRepository postCategoryRepository;

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final LikedPostRepository likedPostRepository;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;


    // 1. 게시글 등록
    @Transactional
    public Post createPost(PostRequestDto postRequestDto, Member currentUser) {
        // 1. 게시판 카테고리 조회
        PostCategory postCategory = postCategoryRepository.findById(postRequestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판 카테고리입니다."));

        // 2. Post 생성 및 저장
        Post post = Post.builder()
                .writer(currentUser)
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .postDate(postRequestDto.getPostDate())
                .postCategory(postCategory)
                .build();

        Post savedPost = postRepository.save(post);

        // 3. HTML 내용에서 이미지 url 추출 -> DB 저장
        List<String> imageUrls = imageService.extractImageUrlsFromContent(postRequestDto.getContent());
        if(!imageUrls.isEmpty()) {
            List<Image> images = imageService.registerImagesFromContent(imageUrls, savedPost);
            post.setImages(images);

            // 대표이미지 설정
            post.setRepresentativeImagePath(images.get(0).getImagePath());

            savedPost = postRepository.save(post);
        }

        return savedPost;
    }


    // 2. 게시글 수정
    @Transactional
    public Post updatePost(Long postId, PostRequestDto postRequestDto, List<MultipartFile> imageFiles, Member currentUser) {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 2. 소유자 확인
        if (!existingPost.getWriter().equals(currentUser)) {
            throw new IllegalArgumentException("해당 게시글을 수정할 권한이 없습니다.");
        }

        // 3. 이미지 업데이트 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            // 기존 이미지 삭제
            imageService.deleteImages(existingPost.getImages());

            // 새로운 이미지 저장
            List<Image> updatedImages = imageService.saveImagesForPost(imageFiles, existingPost);

            // post에 새로운 이미지 설정
            existingPost.setImages(updatedImages);

            // 대표 이미지 설정
            if (!updatedImages.isEmpty()) {
                existingPost.setRepresentativeImagePath(updatedImages.get(0).getImagePath());
            }
        }
        // 새 이미지가 없는 경우 기존 이미지를 유지하므로 별도의 처리 불필요

        // 4. 나머지 속성 업데이트
        existingPost.setTitle(postRequestDto.getTitle());
        existingPost.setContent(postRequestDto.getContent());

        // 5. 카테고리 수정
        if (postRequestDto.getCategoryId() != null) {
            PostCategory postCategory = postCategoryRepository.findById(postRequestDto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판 카테고리입니다."));
            existingPost.setPostCategory(postCategory);
        }
        // 6. 게시글 저장
        return postRepository.save(existingPost);
    }


    // 3. 게시글 상세(단건) 조회
//    public PostResponseDto getPost(Long postNo) {
//        Post post = postRepository.findById(postNo)
//                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
//
//        // 조회수 증가
//        post.increaseHitCount();
//        postRepository.save(post);
//
//        return PostResponseDto.from(post); // DTO 반환
//    }

    @Transactional
    public PostResponseDto getPost(Long postNo, String currentUserId) {
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        post.increaseHitCount();
        postRepository.save(post);

        return PostResponseDto.from(post, currentUserId);
    }


    // 4. 회원별 게시글 조회
    @Transactional(readOnly = true)
    public Slice<PostListDto> getPostsByWriter(String memberId, Pageable pageable) {
        Slice<Post> posts = postRepository.findByWriter_MemberId(memberId, pageable);
        return posts.map(PostListDto::from);
    }


    // 5. 게시글 목록 조회 (메인 화면에 쓰일 것)
    public Slice<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    // 6. 게시글 카테고리별 조회
    @Transactional(readOnly = true)
    public Slice<PostListDto> getPostsByCategory(Long categoryId, Pageable pageable) {
        return postRepository.findByPostCategory_CategoryId(categoryId, pageable)
                .map(PostListDto::from);
    }


    // 7.  게시글 삭제
    @Transactional
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("게시글이 존재하지 않습니다.");
        }

        // 게시글 이미지 삭제 (S3 + DB)
        List<Image> images = imageRepository.findAllByPost_PostId(postId);
        imageService.deleteImages(images);  // S3Uploader도 같이 처리

        // 좋아요 삭제
        likedPostRepository.deleteAllByPost_PostId(postId);

        // 댓글 삭제
        commentRepository.deleteAllByPost_PostId(postId);

        // 게시글 삭제
        postRepository.deleteById(postId);
    }

    @Transactional(readOnly = true)
    public Slice<Post> searchPostsByKeyword(String keyword, Pageable pageable) {
        return postRepository.searchByKeyword(keyword, pageable);
    }

    public Slice<Post> searchPostsByKeywordAndCategory(String keyword, String categoryName, Pageable pageable) {
        return postRepository.searchByKeywordAndCategory(keyword, categoryName, pageable);
    }




}
