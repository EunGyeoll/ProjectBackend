package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.MemberMyPageDto;
import Project.ProjectBackend.service.AuthService;
import Project.ProjectBackend.service.MemberPageService;
import Project.ProjectBackend.service.MemberService;
import Project.ProjectBackend.service.SortService;
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
    private final AuthService authService;
    private final SortService sortService;

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
        // 현재 로그인한 사용자와 조회 대상이 동일한지 확인
        boolean isOwner = authService.getCurrentUser().getMemberId().equals(memberId);

        // SortService를 활용하여 정렬 생성
        Pageable pageableForItems = PageRequest.of(itemsPage, itemsSize, sortService.createSort(itemsSortOption, "item"));
        Pageable pageableForPosts = PageRequest.of(postsPage, postsSize, sortService.createSort(postsSortOption, "post"));
        Pageable pageableForFavoriteItems = PageRequest.of(favoriteItemsPage, favoriteItemsSize, sortService.createSort(favoriteItemsSortOption, "favoriteItem"));
        Pageable pageableForLikedPosts = PageRequest.of(likedPostsPage, likedPostsSize, sortService.createSort(likedPostsSortOption, "likedPost"));

        // 페이지 데이터 조회
        MemberMyPageDto pageData = isOwner ?
                memberPageService.getMyPageData(memberId, pageableForItems, pageableForPosts, pageableForFavoriteItems, pageableForLikedPosts)
                : memberPageService.getMemberPageData(memberId, pageableForItems, pageableForPosts);

        return ResponseEntity.ok(pageData);
    }


}
