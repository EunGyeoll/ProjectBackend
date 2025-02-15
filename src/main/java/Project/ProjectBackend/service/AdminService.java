package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.*;
import Project.ProjectBackend.entity.*;
import Project.ProjectBackend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ImageService imageService;

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final PostRepository postRepository;
    private final OrderRepository orderRepository;
    private final ReportRepository reportRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);



    // 관리자로 회원가입
    @Transactional
    public void signup(MemberSignupRequestDto requestDto, MultipartFile profileImage) {
        // 이메일 중복 체크
        validateDuplicateEmail(requestDto.getEmail());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 먼저 회원(Member) 저장
        Member admin = Member.builder()
                .memberId(requestDto.getMemberId())
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .address(requestDto.getAddress())
                .birthDate(requestDto.getBirthDate())
                .role(Role.ROLE_ADMIN)  // ✅ 관리자 역할 설정
                .phoneNum(requestDto.getPhoneNum())
                .enabled(true)
                .shopIntroduction(requestDto.getShopIntroduction())
                .build();

        // 먼저 데이터베이스에 저장 (회원이 영속 상태가 되어야 함)
        Member savedAdmin = memberRepository.save(admin);

        // 프로필 이미지 저장
        if (profileImage != null && !profileImage.isEmpty()) {
            Image savedProfileImage = imageService.saveImageForProfile(profileImage, savedAdmin);
            savedAdmin.setProfileImageUrl(savedProfileImage.getImagePath());
            savedAdmin.setProfileImage(savedProfileImage);
        }

        // 다시 저장 (프로필 이미지가 설정된 상태로 저장)
        memberRepository.save(savedAdmin);
    }



    private void validateDuplicateEmail(String email) {
        memberRepository.findByEmail(email)
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 이메일입니다.");
                });
    }


    // 회원 정보 수정
    @Transactional
    public void updateAdmin(String memberId, MemberUpdateRequestDto updateRequestDto, MultipartFile profileImage) {
        // 기존 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("관리자 정보를 찾을 수 없습니다."));

        // 전달된 값만 업데이트

        // 이름
        if (updateRequestDto.getName() != null) {
            member.setName(updateRequestDto.getName());
        }
        // 이메일
        if (updateRequestDto.getEmail() != null) {
            member.setEmail(updateRequestDto.getEmail());
        }
        // 생일
        if (updateRequestDto.getBirthDate() != null) {
            member.setBirthDate(updateRequestDto.getBirthDate());
        }
        // 주소
        if (updateRequestDto.getAddress() != null) {
            member.setAddress(updateRequestDto.getAddress());
        }
        // 상점소개
        if (updateRequestDto.getShopIntroduction() != null) {
            member.updateShopIntroduction(updateRequestDto.getShopIntroduction());
        }
        // 프로필사진
        if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 프로필 이미지가 있는 경우 삭제
            if (member.getProfileImage() != null) {
                imageService.deleteImage(member.getProfileImage());
            }
            // 새로운 프로필 이미지 저장
            Image newProfileImage = imageService.saveImageForProfile(profileImage, member);

            // Member 엔티티의 프로필 이미지 정보 업데이트
            member.updateProfileImage(newProfileImage.getImagePath()); // URL 업데이트
            member.setProfileImage(newProfileImage); // Image 엔티티 연관 관계 설정

            logger.info("프로필 이미지가 설정되었습니다: " + newProfileImage.getImagePath());
        }
        // 비밀번호
        if (updateRequestDto.getNewPassword() != null) {
            if (updateRequestDto.getCurrentPassword() == null ||
                    !passwordEncoder.matches(updateRequestDto.getCurrentPassword(), member.getPassword())) {
                throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
            }
            member.updatePassword(passwordEncoder.encode(updateRequestDto.getNewPassword()));
        }

        // 업데이트된 데이터를 저장
        memberRepository.save(member);
    }


    // ===== 회원 관리 =====

    //  1. 회원 전체 목록 조회 (페이징 적용)
    @Transactional(readOnly = true)
    public Slice<MemberSimpleDto> getAllMembers(Pageable pageable) {
        Slice<Member> membersSlice = memberRepository.findAll(pageable);

        return membersSlice.map(MemberSimpleDto::from);
    }


    // 회원 단건 조회
    @Transactional(readOnly = true)
    public MemberSimpleDto getMemberById(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        return MemberSimpleDto.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .enabled(member.isEnabled())
                .birthDate(member.getBirthDate())
                .phoneNum(member.getPhoneNum())
                .address(AddressDto.from(member.getAddress()))
                .build();
    }




    // 2. 회원별 아이템 목록 조회 (페이징 적용)
    @Transactional(readOnly = true)
    public Slice<ItemResponseDto> getMemberItems(String memberId, Pageable pageable) {
        boolean sellerExists = memberRepository.existsById(memberId);
        if (!sellerExists) {
            throw new IllegalArgumentException("판매자를 찾을 수 없습니다.");
        }

        Slice<Item> itemsSlice = itemRepository.findBySeller_MemberId(memberId, pageable);
        return itemsSlice.map(ItemResponseDto::fromForList);
    }

    // 3. 회원별 게시글 목록 조회 (페이징 적용)
    @Transactional(readOnly = true)
    public Slice<PostResponseDto> getMemberPosts(String memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. ID: " + memberId));

        Slice<Post> postsSlice = postRepository.findByWriter_MemberId(memberId, pageable);
        return postsSlice.map(PostResponseDto::fromForList);
    }

    // 4. 회원별 거래 목록 조회 (페이징 적용)
    @Transactional(readOnly = true)
    public Slice<OrderDto> getMemberOrders(String memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. ID: " + memberId));

        Slice<Order> ordersSlice = orderRepository.findByMember(member, pageable);
        return ordersSlice.map(OrderDto::new);
    }

    // 5. 사용자 정보 수정
    @Transactional
    public void updateMember(String memberId, MemberUpdateRequestDto updateRequestDto, MultipartFile profileImage) {
        // 기존 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 전달된 값만 업데이트

        // 이름
        if (updateRequestDto.getName() != null) {
            member.setName(updateRequestDto.getName());
        }
        // 이메일
        if (updateRequestDto.getEmail() != null) {
            member.setEmail(updateRequestDto.getEmail());
        }
        // 생일
        if (updateRequestDto.getBirthDate() != null) {
            member.setBirthDate(updateRequestDto.getBirthDate());
        }
        // 주소
        if (updateRequestDto.getAddress() != null) {
            member.setAddress(updateRequestDto.getAddress());
        }
        // 상점소개
        if (updateRequestDto.getShopIntroduction() != null) {
            member.updateShopIntroduction(updateRequestDto.getShopIntroduction());
        }
        // 프로필사진
        if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 프로필 이미지가 있는 경우 삭제
            if (member.getProfileImage() != null) {
                imageService.deleteImage(member.getProfileImage());
            }
            // 새로운 프로필 이미지 저장
            Image newProfileImage = imageService.saveImageForProfile(profileImage, member);

            // Member 엔티티의 프로필 이미지 정보 업데이트
            member.updateProfileImage(newProfileImage.getImagePath()); // URL 업데이트
            member.setProfileImage(newProfileImage); // Image 엔티티 연관 관계 설정

            logger.info("프로필 이미지가 설정되었습니다: " + newProfileImage.getImagePath());
        }
        // 비밀번호
        if (updateRequestDto.getNewPassword() != null) {
            if (updateRequestDto.getCurrentPassword() == null ||
                    !passwordEncoder.matches(updateRequestDto.getCurrentPassword(), member.getPassword())) {
                throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
            }
            member.updatePassword(passwordEncoder.encode(updateRequestDto.getNewPassword()));
        }

        // 업데이트된 데이터를 저장
        memberRepository.save(member);
    }


    // 회원 탈퇴
    @Transactional
    public boolean deleteMember(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(member != null && member.isEnabled()) {
            member.setEnabled(false); // Soft delete

            memberRepository.save(member);
            return true;
        }
        return false;

    }

    // 회원 권한 복구
    @Transactional
    public void restoreMember(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        member.setEnabled(true);
        memberRepository.save(member);
    }


    public Member findOne(String id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. ID: " + id));
    }



    // ===== 아이템 관리 =====

    /**
     * 6. 전체 상품 목록 조회 (페이징 적용)
     */
    @Transactional(readOnly = true)
    public Slice<ItemResponseDto> getAllItems(Pageable pageable) {
        Slice<Item> itemsSlice = itemRepository.findAll(pageable);
        return itemsSlice.map(ItemResponseDto::fromForList);
    }


    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));
    }

    public PostResponseDto getPost(Long postNo) {
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 조회수 증가
        post.increaseHitCount();
        postRepository.save(post);

        return PostResponseDto.from(post); // DTO 반환
    }
    // 상품 등록
    @Transactional
    public Item createItem(ItemRequestDto itemRequestDto, Member currentUser, List<MultipartFile> imageFiles) {
        logger.info("Creating item: {}", itemRequestDto.getItemName());
        // 작성자(Member) 조회
        Member seller = memberRepository.findById(currentUser.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("판매자가 존재하지 않습니다."));

        Category category = categoryRepository.findById(itemRequestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리입니다."));

        // Item 엔티티 생성
        Item item = Item.builder()
                .seller(currentUser)
                .itemName(itemRequestDto.getItemName())
                .price(itemRequestDto.getPrice())
                .description(itemRequestDto.getDescription())
                .stockQuantity(itemRequestDto.getStockQuantity())
                .category(category)
                .build();

        // 아이템 먼저 저장하여 itemId 확보
        Item savedItem = itemRepository.save(item);
        logger.info("Item created with ID: {}", savedItem.getItemId());

        // 이미지 저장
        if (imageFiles != null && !imageFiles.isEmpty()) {
            logger.info("Saving {} images for item: {}", imageFiles.size(), item.getItemId());
            List<Image> images = imageService.saveImagesForItem(imageFiles, item);
            item.setImages(images);

            // 대표 이미지 설정
            if (!images.isEmpty()) {
                item.setRepresentativeImagePath(images.get(0).getImagePath());
            }

            // 이미지 설정 후 다시 저장
            savedItem = itemRepository.save(savedItem);
            logger.info("Item created with ID: {}", savedItem.getItemId());
        }

        return savedItem;
    }

    // 7. 상품 삭제
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));
        itemRepository.delete(item);
    }

    // 8. 상품 수정
    @Transactional
    public ItemResponseDto updateItem(Long itemId, ItemRequestDto itemRequestDto, List<MultipartFile> imageFiles, Member currentUser) {
        // 1. 아이템 조회
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));

        // 2. 소유자 확인
        if (!existingItem.getSeller().equals(currentUser)) {
            throw new IllegalArgumentException("해당 아이템을 수정할 권한이 없습니다.");
        }

        // 3. 이미지 업데이트 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            // 기존 이미지 삭제
            imageService.deleteImages(existingItem.getImages());

            // 새로운 이미지 저장
            List<Image> updatedImages = imageService.saveImagesForItem(imageFiles, existingItem);

            // 아이템에 새로운 이미지 설정
            existingItem.setImages(updatedImages);

            // 대표 이미지 설정
            if (!updatedImages.isEmpty()) {
                existingItem.setRepresentativeImagePath(updatedImages.get(0).getImagePath());
            }
        }
        // 새 이미지가 없는 경우 기존 이미지를 유지하므로 별도의 처리 불필요

        // 4. 나머지 속성 업데이트
        if (itemRequestDto.getItemName() != null && !itemRequestDto.getItemName().isEmpty()) {
            existingItem.setItemName(itemRequestDto.getItemName());
        }
        if (itemRequestDto.getPrice() != null) {
            existingItem.setPrice(itemRequestDto.getPrice());
        }
        if (itemRequestDto.getDescription() != null && !itemRequestDto.getDescription().isEmpty()) {
            existingItem.setDescription(itemRequestDto.getDescription());
        }
        if (itemRequestDto.getStockQuantity() != null) {
            existingItem.setStockQuantity(itemRequestDto.getStockQuantity());
        }

        // 5. 아이템 저장 및 DTO 반환
        Item updatedItem = itemRepository.save(existingItem);
        return ItemResponseDto.from(updatedItem);
    }




    // ===== 카테고리 관리 =====

    // 9. 카테고리 목록 조회 (계층적)
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        List<Category> allCategories = categoryRepository.findByParentIsNullOrderByCategoryNameAsc();

        // 상위 카테고리만 필터링
        List<Category> parentCategories = allCategories.stream()
                .filter(category -> category.getParent() == null)
                .collect(Collectors.toList());

        // 각 상위 카테고리에 대해 재귀적으로 자식 카테고리를 매핑
        List<CategoryDto> result = parentCategories.stream()
                .map(this::mapCategoryToDto)
                .collect(Collectors.toList());

        return result;
    }

    // 10. 카테고리 추가
    @Transactional
    public CategoryDto addCategory(CategoryDto categoryDto) {
        // 카테고리 중복 체크
        if (categoryRepository.existsByCategoryName(categoryDto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다. 이름: " + categoryDto.getName());
        }

        Category category = new Category();
        category.setCategoryName(categoryDto.getName());

        if (categoryDto.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("상위 카테고리가 존재하지 않습니다. ID: " + categoryDto.getParentId()));

            // 순환 참조 방지
            if (isCircularReference(parentCategory, category)) {
                throw new IllegalArgumentException("순환 참조가 발생합니다. 상위 카테고리를 변경할 수 없습니다.");
            }

            category.setParent(parentCategory);
            parentCategory.getChildren().add(category);
        }

        Category savedCategory = categoryRepository.save(category);

        return mapCategoryToDto(savedCategory);
    }


    // 11. 카테고리 수정 (이름 및 상위 카테고리 변경 가능)
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다. ID: " + categoryId));

        if (categoryDto.getName() != null && !categoryDto.getName().isEmpty()) {
            // 이름 중복 체크
            if (!category.getCategoryName().equals(categoryDto.getName()) &&
                    categoryRepository.existsByCategoryName(categoryDto.getName())) {
                throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다. 이름: " + categoryDto.getName());
            }
            category.setCategoryName(categoryDto.getName());
        }

        if (categoryDto.getParentId() != null) {
            Category newParent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("상위 카테고리가 존재하지 않습니다. ID: " + categoryDto.getParentId()));

            // 순환 참조 방지
            if (isCircularReference(newParent, category)) {
                throw new IllegalArgumentException("순환 참조가 발생합니다. 상위 카테고리를 변경할 수 없습니다.");
            }

            // 기존 부모에서 제거
            if (category.getParent() != null) {
                category.getParent().getChildren().remove(category);
            }

            // 새로운 부모 설정
            category.setParent(newParent);
            newParent.getChildren().add(category);
        } else if (categoryDto.getParentId() == null && category.getParent() != null) {
            // 상위 카테고리를 제거하여 최상위 카테고리로 설정
            category.getParent().getChildren().remove(category);
            category.setParent(null);
        }

        Category savedCategory = categoryRepository.save(category);

        return mapCategoryToDto(savedCategory);
    }


    // 12. 카테고리 삭제
    @Transactional
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("해당 카테고리가 존재하지 않습니다. ID: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }


    // 재귀적으로 카테고리를 DTO로 매핑
    private CategoryDto mapCategoryToDto(Category category) {
        List<CategoryDto> childrenDtos = category.getChildren().stream()
                .map(this::mapCategoryToDto)
                .collect(Collectors.toList());

        return new CategoryDto(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getParent() != null ? category.getParent().getCategoryId() : null,
                childrenDtos
        );
    }


    //순환 참조를 방지하기 위한 메서드
    private boolean isCircularReference(Category parent, Category child) {
        if (parent == null) {
            return false;
        }
        if (parent.equals(child)) {
            return true;
        }
        return isCircularReference(parent.getParent(), child);
    }



     // ===== 포스트 관리 =====

    //10. 전체 게시글 목록 조회 (페이징 적용)
    @Transactional(readOnly = true)
    public Slice<PostResponseDto> getAllPosts(Pageable pageable) {
        Slice<Post> postsSlice = postRepository.findAll(pageable);
        return postsSlice.map(PostResponseDto::fromForList);
    }


    // 게시글 등록
    @Transactional
    public Post createPost(PostRequestDto postRequestDto, Member currentUser, List<MultipartFile> imageFiles) {

        // Post 엔티티 생성
        Post post = Post.builder()
                .writer(currentUser)
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .postDate(postRequestDto.getPostDate())
                .build();

        // post 먼저 저장하여 postId 확보
        Post savedPost = postRepository.save(post);
        logger.info("Item created with ID: {}", savedPost.getPostNo());

        // 이미지 저장
        if (imageFiles != null && !imageFiles.isEmpty()) {
            logger.info("Saving {} images for item: {}", imageFiles.size(), post.getPostNo());
            List<Image> images = imageService.saveImagesForPost(imageFiles, post);
            post.setImages(images);

            // 대표 이미지 설정
            if (!images.isEmpty()) {
                post.setRepresentativeImagePath(images.get(0).getImagePath());
            }

            // 이미지 설정 후 다시 저장
            savedPost = postRepository.save(savedPost);
            logger.info("Post created with ID: {}", savedPost.getPostNo());
        }

        return savedPost;
    }


    // 11. 게시글 삭제
    @Transactional
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + postId);
        }
        postRepository.deleteById(postId);
    }

    // 12. 게시글 수정
    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto updateRequestDto, List<MultipartFile> imageFiles) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + postId));

        // 전달된 값만 업데이트
        if (updateRequestDto.getTitle() != null && !updateRequestDto.getTitle().isEmpty()) {
            post.setTitle(updateRequestDto.getTitle());
            logger.info("게시글 ID {}의 제목이 '{}'으로 업데이트되었습니다.", postId, updateRequestDto.getTitle());
        }

        if (updateRequestDto.getContent() != null && !updateRequestDto.getContent().isEmpty()) {
            post.setContent(updateRequestDto.getContent());
            logger.info("게시글 ID {}의 내용이 업데이트되었습니다.", postId);
        }


        if (imageFiles != null && !imageFiles.isEmpty()) {
            // 기존 이미지 삭제
            imageService.deleteImages(post.getImages());

            // 새로운 이미지 저장
            List<Image> updatedImages = imageService.saveImagesForPost(imageFiles, post);

            // 아이템에 새로운 이미지 설정
            post.setImages(updatedImages);

            // 대표 이미지 설정
            if (!updatedImages.isEmpty()) {
                post.setRepresentativeImagePath(updatedImages.get(0).getImagePath());
            }
        }
        post.setTitle(post.getTitle());
        post.setContent(post.getContent());

        postRepository.save(post);

        return PostResponseDto.from(post);
    }


    // ===== 주문 관리 =====

    // 13. 전체 주문 목록 조회 (페이징 적용)
    @Transactional(readOnly = true)
    public Slice<OrderDto> getAllOrders(Pageable pageable) {
        Slice<Order> ordersSlice = orderRepository.findAll(pageable);
        return ordersSlice.map(OrderDto::new);
    }

    // ===== 신고 관리 =====

    // 14. 상품 신고 목록 조회 (페이징 적용)
    @Transactional(readOnly = true)
    public Slice<ReportResponseDto> getItemReports(Pageable pageable) {
        Slice<Report> reportsSlice = reportRepository.findByReportedEntityType(ReportedEntityType.ITEM, pageable);

        return reportsSlice.map(report -> ReportResponseDto.builder()
                .id(report.getReportId())
                .reportType(report.getReportType())
                .description(report.getDescription())
                .reportedEntityType(report.getReportedEntityType())
                .reportedEntityId(report.getReportedEntityId())
                .reportedEntityName(report.getReportedEntityName())
                .reporterMemberId(report.getReporter() != null ? report.getReporter().getMemberId() : null)
                .build());
    }

    // 15. 게시글 신고 목록 조회 (페이징 적용)
    @Transactional(readOnly = true)
    public Slice<ReportResponseDto> getPostReports(Pageable pageable) {
        Slice<Report> reportsSlice = reportRepository.findByReportedEntityType(ReportedEntityType.POST, pageable);
        return reportsSlice.map(report -> ReportResponseDto.builder()
                .id(report.getReportId())
                .reportType(report.getReportType())
                .description(report.getDescription())
                .reportedEntityType(report.getReportedEntityType())
                .reportedEntityId(report.getReportedEntityId())
                .reportedEntityName(report.getReportedEntityName())
                .reporterMemberId(report.getReporter() != null ? report.getReporter().getMemberId() : null)
                .build());
    }

    // 16. 거래 신고 목록 조회 (페이징 적용)
    @Transactional(readOnly = true)
    public Slice<ReportResponseDto> getOrderReports(Pageable pageable) {
        Slice<Report> reportsSlice = reportRepository.findByReportedEntityType(ReportedEntityType.ORDER, pageable);
        return reportsSlice.map(report -> ReportResponseDto.builder()
                .id(report.getReportId())
                .reportType(report.getReportType())
                .description(report.getDescription())
                .reportedEntityId(report.getReportedEntityId())
                .reportedEntityName(report.getReportedEntityName())
                .reporterMemberId(report.getReporter() != null ? report.getReporter().getMemberId() : null)
                .build());
    }

    // 회원 신고 목록 조회 (페이징 적용)
    @Transactional(readOnly = true)
    public Slice<ReportResponseDto> getMemberReports(Pageable pageable) {
        Slice<Report> reportsSlice = reportRepository.findByReportedEntityType(ReportedEntityType.MEMBER, pageable);

        return reportsSlice.map(report -> ReportResponseDto.builder()
                .id(report.getReportId())
                .reportType(report.getReportType())
                .description(report.getDescription())
                .reportedEntityType(report.getReportedEntityType())
                .reportedEntityId(report.getReportedEntityId())
                .reporterMemberId(report.getReporter() != null ? report.getReporter().getMemberId() : null)
                .build());
    }


    // ====== 댓글 관리 ======
// 1-1. 댓글 작성
    @Transactional
    public Comment addComment(Long postId, CommentRequestDto requestDto, Member currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 현재 사용자를 작성자로 설정
        Comment comment = Comment.builder()
                .post(post)
                .writer(currentUser)
                .content(requestDto.getContent())
                .build();

        return commentRepository.save(comment);
    }

    // 1-2. 대댓글 작성
    @Transactional
    public Comment addReply(Long postNo, CommentRequestDto requestDto, Member currentUser) {
        // 게시글과 부모 댓글 조회
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        Comment parentComment = commentRepository.findById(requestDto.getParentCommentId())
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));

        // 대댓글 생성
        Comment reply = Comment.builder()
                .post(post)
                .writer(currentUser)
                .content(requestDto.getContent())
                .parentComment(parentComment)
                .build();

        // 부모 댓글에 자식 댓글 추가
        parentComment.addChildComment(reply);

        // 대댓글 저장
        return commentRepository.save(reply);
    }


    // 2. 댓글 수정
    @Transactional
    public Comment updateComment(Long commentId, CommentUpdateRequestDto updateRequestDto, Member currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getWriter().equals(currentUser)) {
            throw new IllegalArgumentException("해당 댓글을 수정할 권한이 없습니다.");
        }

        comment.setContent(updateRequestDto.getContent());

        return commentRepository.save(comment);
    }



    // 3. 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Member currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 댓글 소유자 또는 관리자 권한 확인
        if (!comment.getWriter().equals(currentUser) && !currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new IllegalArgumentException("해당 댓글을 삭제할 권한이 없습니다.");
        }

        // 댓글 삭제
        comment.markAsdeleted();
        commentRepository.save(comment);
    }


    // 4. 특정 게시글의 댓글, 대댓글 조회
    @Transactional(readOnly = true)
    public Slice<Comment> getCommentsByPost(Long postNo, Pageable pageable) {

        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        return commentRepository.findByPostOrderByCommentDateAsc(post, pageable);
    }


    // ================== 채팅 =================

    // ✅ 전체 채팅 내역 조회
    public Slice<ChatListDtoForAdmin> getAllChatList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findAllChats(pageable)
                .map(message -> ChatListDtoForAdmin.builder()
                        .sender(message.getSender())
                        .receiver(message.getReceiver())
                        .lastMessage(message.getContent())
                        .timestamp(message.getTimestamp())
                        .build()
                );
    }

    // ✅ 특정 사용자의 채팅 내역 조회
    public Slice<ChatListDto> getMemberChatList(String memberId, int page, int size) {
        return chatService.getMemberChatList(memberId, page, size);
    }


    // ✅ 특정 사용자 간의 채팅 내역 조회
    public Slice<ChatHistoryDto> getMessagesBetweenUsers(String sender, String receiver, int page, int size) {
        return chatService.getMessagesBetweenUsers(sender, receiver, page, size);
    }

    // ✅ 특정 채팅방(roomId) 내 채팅 내역 조회
    public Slice<ChatHistoryDto> getChatHistoryByRoomId(String roomId, int page, int size) {
        return chatService.getMessagesByRoomId(roomId, page, size);
    }
}