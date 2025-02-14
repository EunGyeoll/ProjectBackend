package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.*;
import Project.ProjectBackend.entity.Comment;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Post;
import Project.ProjectBackend.service.AdminService;
import Project.ProjectBackend.service.AuthService;
import Project.ProjectBackend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
//@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;
    private final ChatService chatService;

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

/*    목차
    회원별 - 회원 전체 목록 조회, 단건 조회, 회원별 아이템 목록, 회원별 게시글 목록, 회원별 거래 목록, 회원 정보 수정, 탈퇴, 회원 복구
    아이템 - 전체 목록 조회, 단건 조회, 등록, 삭제, 수정,
    아이템 카테고리 - 목록 조회, 단건 조회, 등록, 삭제, 수정
    게시글 - 전체 목록 조회, 등록, 수정, 삭제
    주문 - 전체 목록 조회
    신고 -  상품 신고 목록 조회, 게시글 신고 목록 조회, 거래 신고 목록 조회

    */

    // 관리자로 회원가입
    @PostMapping("/admin/new")
    public ResponseEntity<?> signup(
            @RequestPart(value = "memberData") @Valid MemberSignupRequestDto requestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        adminService.signup(requestDto, profileImage);
        return ResponseEntity.ok("관리자로 회원가입 성공!");
    }

    // 관리자 정보 수정
    @PatchMapping("/admin/update")
    public ResponseEntity<?> updateAdmin(
            Authentication authentication,
            @RequestPart(value = "memberData") @Valid MemberUpdateRequestDto updateRequestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        String memberId = authentication.getName();
        adminService.updateAdmin(memberId, updateRequestDto, profileImage);

        Member updatedAdmin = adminService.findOne(memberId);
        return ResponseEntity.ok(new MemberController.UpdateMemberResponse(updatedAdmin.getMemberId(), updatedAdmin.getName()));
    }

    // ===== 회원 관리 =====

    //  회원 전체 목록 조회 (페이징 적용)
    @GetMapping("/admin/members/list")
    public ResponseEntity<Slice<MemberSimpleDto>> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder = getSortOrder(sortOption, "member");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<MemberSimpleDto> membersSlice = adminService.getAllMembers(pageable);
        return ResponseEntity.ok(membersSlice);
    }


    // 회원정보 간단 조회
    @GetMapping("/admin/members/{id}")
    public ResponseEntity<MemberSimpleDto> getMemberById(@PathVariable("id") String memberId) {
        MemberSimpleDto memberSimpleDto = adminService.getMemberById(memberId);
        return ResponseEntity.ok(memberSimpleDto);
    }


    //  회원별 아이템 목록 조회 (페이징 적용)
    @GetMapping("/admin/members/{id}/items")
    public ResponseEntity<Slice<ItemResponseDto>> getMemberItems(
            @PathVariable("id") String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder = getSortOrder(sortOption, "item");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<ItemResponseDto> itemsSlice = adminService.getMemberItems(memberId, pageable);
        return ResponseEntity.ok(itemsSlice);
    }

    //  회원별 게시글 목록 조회 (페이징 적용)
    @GetMapping("/admin/members/{id}/posts")
    public ResponseEntity<Slice<PostResponseDto>> getMemberPosts(
            @PathVariable("id") String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder = getSortOrder(sortOption, "post");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<PostResponseDto> postsSlice = adminService.getMemberPosts(memberId, pageable);
        return ResponseEntity.ok(postsSlice);
    }

    //  회원별 거래 목록 조회 (페이징 적용)
    @GetMapping("/admin/members/{id}/orders")
    public ResponseEntity<Slice<OrderDto>> getMemberTransactions(
            @PathVariable("id") String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder = getSortOrder(sortOption, "order");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<OrderDto> transactionsSlice = adminService.getMemberOrders(memberId, pageable);
        return ResponseEntity.ok(transactionsSlice);
    }

    // 사용자 정보 수정
    @PutMapping("/admin/members/{id}/update")
    public ResponseEntity<String> updateMember(
            @PathVariable("id") String memberId,
            @RequestBody @Valid MemberUpdateRequestDto updateRequestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        adminService.updateMember(memberId, updateRequestDto, profileImage);
        return ResponseEntity.ok("회원 정보가 업데이트되었습니다.");
    }


    // 회원 탈퇴
    @DeleteMapping("/admin/members/delete/{memberId}")
    public ResponseEntity<?> deleteMember(@PathVariable String memberId, Authentication authentication) {
        String adminId = authentication.getName();

        if(adminId.equals(memberId)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자는 자신의 계정을 삭제할 수 없습니다");
        }

        boolean isDeleted = adminService.deleteMember(memberId);

        if(isDeleted){
            return ResponseEntity.ok("회원을 정상적으로 탈퇴시켰습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원을 찾을 수 없습니다.");
        }
    }


    // 회원 권한 복구
    @PutMapping("/admin/members/restore-role/{memberId}")
    public ResponseEntity<?> restoreRole(@PathVariable String memberId) {
        // 회원 찾기
        Member member = adminService.findOne(memberId);
        if (member == null) {
            return ResponseEntity.status(404).body("회원을 찾을 수 없습니다.");
        }

        // enabled를 true로
        adminService.restoreMember(memberId);

        return ResponseEntity.ok("회원 권한이 복구되었습니다.");
    }


    // ===== 아이템 관리 =====

    // 전체 상품 목록 조회 (페이징 적용)
    @GetMapping("/admin/items/list")
    public ResponseEntity<Slice<ItemResponseDto>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder = getSortOrder(sortOption,"item");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<ItemResponseDto> itemsSlice = adminService.getAllItems(pageable);
        return ResponseEntity.ok(itemsSlice);
    }

    // 아이템 상세(단건) 조회
    @GetMapping("/admin/items/{itemId}")
    public ResponseEntity<ItemResponseDto> getItem(@PathVariable Long itemId) {
        Item item = adminService.getItemById(itemId);
        ItemResponseDto responseDto = ItemResponseDto.from(item);
        return ResponseEntity.ok(responseDto);
    }

    // 아이템 등록
    @PostMapping("/admin/items/new")
    public ResponseEntity<ItemResponseDto> createItemByAdmin(
            @RequestPart(value = "itemData") @Valid ItemRequestDto itemRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        Member currentUser = authService.getCurrentUser();

        Item createdItem = adminService.createItem(itemRequestDto, currentUser, imageFiles); // 이미지 파일 전달
        return ResponseEntity.ok(ItemResponseDto.from(createdItem));
    }

    //  아이템 삭제
    @DeleteMapping("/admin/items/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable("id") Long itemId) {
        adminService.deleteItem(itemId);
        return ResponseEntity.ok("상품이 삭제되었습니다.");
    }

    // 아이템 수정
    @PatchMapping("/admin/items/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(
            @PathVariable Long itemId,
            @RequestPart("itemData") @Valid ItemRequestDto itemRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        // 현재 로그인된 사용자 가져오기
        Member currentUser = authService.getCurrentUser();

        // 서비스 계층 호출, 이미지 파일 전달
        ItemResponseDto updatedItem = adminService.updateItem(itemId, itemRequestDto, imageFiles, currentUser);

        return ResponseEntity.ok(updatedItem);
    }


    // ===== 아이템의 카테고리 관리 =====

    //  카테고리 목록 조회 (계층적)
    @GetMapping("/admin/categories/list")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = adminService.getAllCategories();
        logger.info("관리자 {} 가 모든 카테고리 목록을 조회했습니다.", authService.getCurrentUser().getMemberId());

        return ResponseEntity.ok(categories);
    }

    // 카테고리 추가
    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDto> addCategory(
            @RequestBody @Valid CategoryDto categoryDto ) {
        CategoryDto category = adminService.addCategory(categoryDto);
        logger.info("카테고리 {} 을 추가했습니다.", categoryDto.getName());
        return ResponseEntity.ok(category);
    }


    // 카테고리 수정 (이름 및 상위 카테고리 변경 가능)
    @PatchMapping("/admin/categories/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable("id") Long categoryId,
            @RequestBody @Valid CategoryDto categoryDto) {
        CategoryDto updatedCategory = adminService.updateCategory(categoryId, categoryDto);
        logger.info("카테고리 {}을 수정했습니다.", updatedCategory.getName());
        return ResponseEntity.ok(updatedCategory);
    }

    //  카테고리 삭제
    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable("id") Long categoryId) {
        adminService.deleteCategory(categoryId);
        logger.info("카테고리 ID {}을 삭제했습니다.", categoryId);
        return ResponseEntity.ok("카테고리가 삭제되었습니다.");
    }


    // ===== 포스트 관리 =====

    // 전체 게시글 목록 조회 (페이징 적용)
    @GetMapping("/admin/posts/list")
    public ResponseEntity<Slice<PostResponseDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder = getSortOrder(sortOption, "post");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<PostResponseDto> postsSlice = adminService.getAllPosts(pageable);
        return ResponseEntity.ok(postsSlice);
    }

    // 게시글 상세(단건) 조회
    @GetMapping("/admin/posts/{postNo}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postNo) {
        PostResponseDto post = adminService.getPost(postNo);
        return ResponseEntity.ok(post);
    }


    // 게시글 등록
    @PostMapping("/admin/posts/new")
    public ResponseEntity<PostResponseDto> createPost(
            @RequestPart(value = "itemData") @Valid PostRequestDto postRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        Member currentUser = authService.getCurrentUser();

        Post createdPost = adminService.createPost(postRequestDto, currentUser, imageFiles);
        return ResponseEntity.ok(PostResponseDto.from(createdPost));
    }


    // 게시글 삭제
    @DeleteMapping("/admin/posts/{id}")
    public ResponseEntity<String> deletePost(@PathVariable("id") Long postId) {
        adminService.deletePost(postId);
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
    }

    // 게시글 수정
    @PatchMapping("/admin/posts/{id}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable("id") Long postId,
            @RequestPart("postData") @Valid PostRequestDto postRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles)  {

        // 현재 로그인된 사용자 가져오기
        Member currentUser = authService.getCurrentUser();

        PostResponseDto updatedPost = adminService.updatePost(postId, postRequestDto, imageFiles);
        return ResponseEntity.ok(updatedPost);
    }





    // ===== 주문 관리 =====


    //  전체 주문 목록 조회 (페이징 적용)
    @GetMapping("/admin/orders")
    public ResponseEntity<Slice<OrderDto>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder = getSortOrder(sortOption, "order");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<OrderDto> ordersSlice = adminService.getAllOrders(pageable);
        return ResponseEntity.ok(ordersSlice);
    }





    // ===== 신고 관리 =====


    //  상품 신고 목록 조회 (페이징 적용)
    @GetMapping("/admin/reports/items")
    public ResponseEntity<Slice<ReportResponseDto>> getItemReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder = getSortOrder(sortOption,"report");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<ReportResponseDto> reportsSlice = adminService.getItemReports(pageable);
        return ResponseEntity.ok(reportsSlice);
    }


    //  게시글 신고 목록 조회 (페이징 적용)
    @GetMapping("/admin/reports/posts")
    public ResponseEntity<Slice<ReportResponseDto>> getPostReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder = getSortOrder(sortOption,"report");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<ReportResponseDto> reportsSlice = adminService.getPostReports(pageable);
        return ResponseEntity.ok(reportsSlice);
    }

    //  주문(거래) 신고 목록 조회 (페이징 적용)
    @GetMapping("/admin/reports/orders")
    public ResponseEntity<Slice<ReportResponseDto>> getOrderReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder = getSortOrder(sortOption,"report");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<ReportResponseDto> reportsSlice = adminService.getOrderReports(pageable);
        return ResponseEntity.ok(reportsSlice);
    }


    //  회원 신고 목록 조회 (페이징 적용)
    @GetMapping("/admin/reports/members")
    public ResponseEntity<Slice<ReportResponseDto>> getMemberReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder = getSortOrder(sortOption,"report");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<ReportResponseDto> reportsSlice = adminService.getMemberReports(pageable);
        return ResponseEntity.ok(reportsSlice);
    }


    // ======== 댓글 ============

    // 1. 댓글 및 대댓글 작성
    @PostMapping("/admin/comments/{postNo}")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postNo, @RequestBody @Valid  CommentRequestDto commentRequestDto) {
        Member currentUser = authService.getCurrentUser();

        Comment comment;

        if(commentRequestDto.getParentCommentId() == null) {
            // 댓글 작성
            comment = adminService.addComment(postNo, commentRequestDto, currentUser);
        } else {
            // 대댓글 작성
            comment = adminService.addReply(postNo, commentRequestDto, currentUser);
        }
        return ResponseEntity.ok(new CommentResponseDto(comment));
    }



    // 2. 댓글 수정
    @PatchMapping("/admin/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequestDto updateRequestDto) {

        Member currentUser = authService.getCurrentUser();

        Comment updatedComment = adminService.updateComment(commentId, updateRequestDto, currentUser);

        return ResponseEntity.ok(new CommentResponseDto(updatedComment));
    }



    // 3. 댓글 삭제
    @DeleteMapping("/admin/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId) {

        Member currentUser = authService.getCurrentUser();

        adminService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
    }


    // 4. 게시글의 댓글, 대댓글 조회
    @GetMapping("/admin/comments/{postNo}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(
            @PathVariable Long postNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable = PageRequest.of(page,size);

        // 댓글 목록 조회
        Slice<Comment> comments = adminService.getCommentsByPost(postNo, pageable);


        // Comment -> CommentResponseDto 변환
        List<CommentResponseDto> commentResponseDtos = comments.getContent()
                .stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(commentResponseDtos);
    }


    // ================== 채팅 ==================

    // 모든 채팅 목록 조회
    @GetMapping("/admin/chat/list")
    public Slice<ChatListDto> getAllChatList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("관리자 - 모든 채팅 목록 조회");
        return adminService.getAllChats(page, size);
    }

    // 특정 사용자의 채팅 목록 조회
    @GetMapping("/admin/chat/{memberId}")
    public Slice<ChatListDto> getMemberChatList(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("관리자 - {}의 채팅 목록 조회", memberId);
        return adminService.getMemberChatList(memberId, page, size);
    }

    // 특정 사용자간의 채팅 내역 조회
    @GetMapping("/admin/chat/history/{sender}/{receiver}")
    public Slice<ChatHistoryDto> getChatHistory(
            @PathVariable String sender,
            @PathVariable String receiver,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("관리자 - {} & {} 간의 채팅 내역 조회", sender, receiver);
        return adminService.getMemberChatHistory(sender, receiver, page, size);
    }

    // ✅ 특정 채팅방(roomId) 내 대화 조회
    @GetMapping("/admin/chat/room/{roomId}")
    public Slice<ChatHistoryDto> getChatHistoryByRoomId(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("관리자 - 채팅방 {} 내 대화 조회", roomId);
        return adminService.getChatHistoryByRoomId(roomId, page, size);
    }

    // ===== Helper Method =====

    // 정렬 옵션에 따른 Sort 객체 생성

    /**
     * 엔티티 유형에 따라 정렬 옵션을 반환함.
     *
     * @param sortOption 정렬 옵션 (e.g., "popular", "lowprice", "highprice", "latest")
     * @param entityType 정렬할 엔티티 유형 (e.g., "member", "item", "post", "order", "report")
     * @return Sort 객체
     */
    private Sort getSortOrder(String sortOption, String entityType) {
        switch (sortOption.toLowerCase()) {
            case "popular":
                if ("item".equals(entityType)) {
                    return Sort.by(Sort.Direction.DESC, "favoriteCount");
                }
                // 다른 엔티티의 "popular" 정렬 로직 추가
                break;
            case "lowprice":
                if ("item".equals(entityType)) {
                    return Sort.by(Sort.Direction.ASC, "price");
                }
                break;
            case "highprice":
                if ("item".equals(entityType)) {
                    return Sort.by(Sort.Direction.DESC, "price");
                }
                break;
            case "latest":
            default:
                switch (entityType.toLowerCase()) {
                    case "member":
                        return Sort.by(Sort.Direction.DESC, "registrationDate");
                    case "item":
                        return Sort.by(Sort.Direction.DESC, "itemDate");
                    case "post":
                        return Sort.by(Sort.Direction.DESC, "postDate");
                    case "order":
                        return Sort.by(Sort.Direction.DESC, "orderDate");
                    case "report":
                        return Sort.by(Sort.Direction.DESC, "reportDate");
                    default:
                        return Sort.unsorted();
                }
        }
        // 기본적으로 unsorted 반환
        return Sort.unsorted();
    }
}
