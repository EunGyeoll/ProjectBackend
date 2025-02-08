package Project.ProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@DynamicUpdate
public class Member {
    @Id
    @Column(name = "member_id")
    private String memberId;
    @NotEmpty
    private String password;
    @NotEmpty
    private String name;

    @CreationTimestamp
    @Column(updatable = false) // 수정 시 값 변경되지 않도록 설정
    private LocalDateTime registrationDate;

    @NotNull
    @Enumerated(EnumType.STRING) // Enum 값이 숫자로 저장되는 것을 막기 위해
    private Role role;

    @Column(name = "enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean enabled = true; // 권한 있는지

    @NotEmpty
    private String email;

    @Column(length = 100)  //  최대 100자 제한
    private String shopIntroduction;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Image profileImage;

    @Column
    private String profileImageUrl;


    @NotNull
    @Column(name = "birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotEmpty
    @Column(name = "phone_num")
    private String phoneNum;
    // 숫자 필드엔 @NotEmpty 대신 @NotNull 또는 @Positive, @Min, @Max 등의 애노테이션을 사용

    @Embedded
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true) // 양방향 관계 설정
    @JsonIgnore
    private List<Post> posts = new ArrayList<>(); // 작성한 게시글 목록

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // 직렬화에서 제외
    private List<Item> items = new ArrayList<>(); // 회원이 등록한 상품 목록

    // 찜한 상품
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FavoriteItem> favoriteItems = new ArrayList<>(); // 사용자의 관심 상품 목록

    // 좋아요한 게시글
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LikedPost> likedPosts = new ArrayList<>(); // 사용자의 관심 상품 목록

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberCoupon> memberCoupons = new ArrayList<>(); // 중간 엔티티


    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> writtenReviews = new ArrayList<>(); // 작성한 리뷰

    @OneToMany(mappedBy = "storeOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> receivedReviews = new ArrayList<>(); // 받은 리뷰


    public void addItem(Item item) {
        items.add(item);
        item.setSeller(this); // 연관 관계 설정
    }

    public void removeItem(Item item) {
        items.remove(item);
        item.setSeller(null); // 연관 관계 해제
    }

    public void updateShopIntroduction(String shopIntroduction) {
        if (shopIntroduction != null && shopIntroduction.length() > 50) {
            throw new IllegalArgumentException("상점 소개는 최대 50자까지 입력할 수 있습니다.");
        }
        this.shopIntroduction = shopIntroduction;
    }

    // 프로필 이미지 업데이트
    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // 비밀번호 업데이트 (암호화 적용 필요)
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }


    @Builder
    public Member(String memberId, String password, String name, String email, LocalDate birthDate, Role role, Address address, String phoneNum, boolean enabled, String shopIntroduction, String profileImageUrl      ) {
        this.memberId = memberId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.birthDate = birthDate;
        this.role = role;
        this.phoneNum = phoneNum;
        this.address = address;
        this.enabled = enabled;
        this.shopIntroduction = shopIntroduction;
        this.profileImageUrl = profileImageUrl;
    }

}
