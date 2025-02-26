package Project.ProjectBackend.service;


import Project.ProjectBackend.dto.LikedPostListDto;
import Project.ProjectBackend.entity.*;
import Project.ProjectBackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class LikedPostService {
    private final LikedPostRepository likedPostRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 찜한 상품에 추가
    @Transactional
    public void addLike(String memberId, Long postNo) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (likedPostRepository.existsByMemberAndPost(member, post)) {
            throw new IllegalStateException("이미 좋아요를 누르신 게시글입니다.");
        }

        // favorite 저장
        LikedPost likedPost = new LikedPost(member, post);
        likedPostRepository.save(likedPost);

        // post의 favoriteCount 증가
        post.increaseLikeCount();
        postRepository.save(post);
    }

    // 게시글 좋아요 삭제
    @Transactional
    public void removeLike(String memberId, Long postNo) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // LikedPost 삭제
        LikedPost likedPost = likedPostRepository.findByMemberAndPost(member, post)
                .orElseThrow(() -> new IllegalArgumentException("좋아요를 누른 적이 없는 게시글입니다."));
        likedPostRepository.delete(likedPost);

        // Post의 좋아요 수 감소
        post.decreaseLikeCount();
        postRepository.save(post);
    }

    // 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    @Transactional(readOnly = true)
    public boolean isLiked(String memberId, Long postNo) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return likedPostRepository.existsByMemberAndPost(member, post);
    }

    // 특정 사용자가 좋아요한 게시글 목록 조회
    @Transactional(readOnly = true)
    public Slice<LikedPostListDto> getLikedPostsByMember(String memberId, Pageable pageable) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        return likedPostRepository.findByMember(member, pageable)
                .map(LikedPostListDto::from);
    }



}
