package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.MemberMyPageDto;
import Project.ProjectBackend.service.MemberPageService;
import Project.ProjectBackend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
public class MemberPageController {

    private final MemberService memberService;
    private final MemberPageService memberPageService;


    // 회원 페이지 단건 조회 (누구나 조회 가능 / 자신의 페이지일 경우에만 name, address, phoneNum 같은 상세한 개인정보 조회 & 수정 버튼 표시 )
    @GetMapping("/memberpage/{memberId}")
    public ResponseEntity<MemberMyPageDto> getMemberPage(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "0") int itemsPage,
            @RequestParam(defaultValue = "10") int itemsSize,
            @RequestParam(defaultValue = "latest") String itemsSortOption,
            @RequestParam(defaultValue = "0") int postsPage,
            @RequestParam(defaultValue = "10") int postsSize,
            @RequestParam(defaultValue = "latest") String postsSortOption,
            @RequestParam(defaultValue = "0") int favoriteItemsPage,
            @RequestParam(defaultValue = "10") int favoriteItemsSize,
            @RequestParam(defaultValue = "latest") String favoriteItemsSortOption,
            @RequestParam(defaultValue = "0") int likedPostsPage,
            @RequestParam(defaultValue = "10") int likedPostsSize,
            @RequestParam(defaultValue = "latest") String likedPostsSortOption
    ) {

        // 현재 로그인한 사용자 ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentMemberId = authentication.getName();

        // 조회 대상과 현재 사용자가 동일한지 확인
        boolean isOwner = currentMemberId.equals(memberId);

        // 아이템
        Sort itemsSortOrder = getSortOrder(itemsSortOption, "item");
        Pageable pageableForItems = PageRequest.of(itemsPage, itemsSize, itemsSortOrder);

        // 포스트
        Sort postsSortOrder = getSortOrder(postsSortOption, "post");
        Pageable pageableForPosts = PageRequest.of(postsPage, postsSize, postsSortOrder);

        // 찜한 아이템
        Sort favoriteItemsSortOrder = getSortOrder(favoriteItemsSortOption, "favoriteItem");
        Pageable pageableForFavoriteItems = PageRequest.of(favoriteItemsPage, favoriteItemsSize, favoriteItemsSortOrder);

        // 좋아요한 포스트
        Sort likedPostsSortOrder = getSortOrder(likedPostsSortOption, "likedPost");
        Pageable pageableForLikedPosts = PageRequest.of(likedPostsPage, likedPostsSize, likedPostsSortOrder);

        if (isOwner) {
            // 자신의 페이지 데이터 조회
            MemberMyPageDto myPageData = memberPageService.getMyPageData(memberId, pageableForItems, pageableForPosts, pageableForFavoriteItems, pageableForLikedPosts);
            return ResponseEntity.ok(myPageData);
        } else {
            // 다른 사용자의 페이지 데이터 조회
            MemberMyPageDto memberPageData = memberPageService.getMemberPageData(memberId, pageableForItems, pageableForPosts);
            return ResponseEntity.ok(memberPageData);
        }
    }



    // 정렬 옵션에 따른 Sort 객체 생성
    private Sort getSortOrder(String sortOption, String entityType) {
        switch (entityType.toLowerCase()) {
            case "item":
                switch (sortOption.toLowerCase()) {
                    case "popular":
                        return Sort.by(Sort.Direction.DESC, "favoriteCount");
                    case "lowprice":
                        return Sort.by(Sort.Direction.ASC, "price");
                    case "highprice":
                        return Sort.by(Sort.Direction.DESC, "price");
                    case "latest":
                    default:
                        return Sort.by(Sort.Direction.DESC, "itemDate"); // 최신 아이템 정렬
                }
            case "post":
                switch (sortOption.toLowerCase()) {
                    case "popular":
                        return Sort.by(Sort.Direction.DESC, "likeCount");
                    case "latest":
                    default:
                        return Sort.by(Sort.Direction.DESC, "postDate"); // 최신 게시글 정렬
                }
            case "favoriteItem":
            case "likedPost":
                return Sort.by(Sort.Direction.DESC, "createdAt"); // 찜한 상품 및 좋아요한 게시글 최신순
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt"); // 기본 정렬
        }
    }

}
