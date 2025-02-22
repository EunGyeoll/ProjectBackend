package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.*;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MemberPageService {

    private final ItemService itemService;
    private final PostService postService;
    private final FavoriteItemService favoriteItemService;
    private final LikedPostService likedPostService;
    private final AuthService authService;


    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final PostRepository postRepository;
    private final FavoriteRepository favoriteRepository;
    private final LikedPostRepository likedPostRepository;



    // 마이페이지 조회 (로그인한 사용자의 페이지)
    @Transactional(readOnly = true)
    public MemberMyPageDto getMyPageData(
            String targetMemberId, Pageable pageableForItems, Pageable pageableForPosts, Pageable pageableForFavoriteItems, Pageable pageableForLikedPosts) {

        String currentMemberId = authService.getCurrentUser().getMemberId();

        // 회원 정보 조회
        Member member = memberRepository.findByMemberId(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        boolean isOwnProfile = currentMemberId.equals(targetMemberId);


        Slice<ItemResponseDto> itemsSlice = itemService.getItemsBySeller(member.getMemberId(), pageableForItems);
        Slice<PostResponseDto> postsSlice = postService.getPostsByWriter(member.getMemberId(), pageableForPosts);
        Slice<FavoriteItemDto> favoriteItemsSlice = favoriteItemService.getFavoriteItemsByMember(member.getMemberId(), pageableForFavoriteItems);
        Slice<LikedPostDto> likedPostsSlice = likedPostService.getLikedPostsByMember(member.getMemberId(), pageableForLikedPosts);

        return MemberMyPageDto.from(
                member, isOwnProfile,
                itemsSlice.getContent(), itemsSlice.hasNext(),
                postsSlice.getContent(), postsSlice.hasNext(),
                favoriteItemsSlice.getContent(), favoriteItemsSlice.hasNext(),
                likedPostsSlice.getContent(), likedPostsSlice.hasNext()
        );
    }


    // 타인의 페이지 조회 (찜한 상품 및 좋아요한 게시글 제외)
    @Transactional(readOnly = true)
    public MemberMyPageDto getMemberPageData(
            String targetMemberId, Pageable pageableForItems, Pageable pageableForPosts) {

        // 회원 정보 조회
        Member member = memberRepository.findByMemberId(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // Service 계층에서 데이터 가져오기
        Slice<ItemResponseDto> itemsSlice = itemService.getItemsBySeller(member.getMemberId(), pageableForItems);
        Slice<PostResponseDto> postsSlice = postService.getPostsByWriter(member.getMemberId(), pageableForPosts);

        return MemberMyPageDto.from(
                member,
                false,  // 타인의 페이지
                itemsSlice.getContent(), itemsSlice.hasNext(),
                postsSlice.getContent(), postsSlice.hasNext(),
                new ArrayList<>(), false,  // 찜한 상품 제외
                new ArrayList<>(), false  // 좋아요한 게시글 제외
        );
    }
}
