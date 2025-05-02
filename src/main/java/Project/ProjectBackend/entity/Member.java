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
@Getter
@NoArgsConstructor
@DynamicUpdate
@Table(name="member")
public class Member {
    @Id
    @Column(name = "member_id")
    @Setter
    private String memberId;

    @NotEmpty
    private String password;

    @NotEmpty
    @Setter
    @Column(name="member_name")
    private String memberName;

    @Column(name="nick_name", nullable = false, unique = true)
    @Setter
    private String nickName;

    @CreationTimestamp
    @Column(updatable = false) // 수정 시 값 변경되지 않도록 설정
    private LocalDateTime registrationDate;

    @NotNull
    @Enumerated(EnumType.STRING) // Enum 값이 숫자로 저장되는 것을 막기 위해
    @Setter
    private Role role;

    @Column(name = "enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Setter
    private boolean enabled = true; // 권한 있는지

    @NotEmpty
    @Setter
    private String email;

    @Column(length = 100)  //  최대 100자 제한
    @Setter
    private String shopIntroduction;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
    private Image profileImage;

    @Column
    @Setter
    private String profileImageUrl;


    @NotNull
    @Column(name = "birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Setter
    private LocalDate birthDate;

    @NotEmpty
    @Column(name = "phone_num")
    @Setter
    private String phoneNum;
    // 숫자 필드엔 @NotEmpty 대신 @NotNull 또는 @Positive, @Min, @Max 등의 애노테이션을 사용

    @Embedded
    @Setter
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "member")
    @Setter
    private List<Orders> orders = new ArrayList<>();

    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true) // 양방향 관계 설정
    @JsonIgnore
    @Setter
    private List<Post> posts = new ArrayList<>(); // 작성한 게시글 목록

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // 직렬화에서 제외
    @Setter
    private List<Item> items = new ArrayList<>(); // 회원이 등록한 상품 목록

    // 찜한 상품
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Setter
    private List<FavoriteItem> favoriteItems = new ArrayList<>(); // 사용자의 관심 상품 목록

    // 좋아요한 게시글
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Setter
    private List<LikedPost> likedPosts = new ArrayList<>(); // 사용자의 관심 상품 목록

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
    private List<MemberCoupon> memberCoupons = new ArrayList<>(); // 중간 엔티티


    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
    private List<Review> writtenReviews = new ArrayList<>(); // 작성한 리뷰

    @OneToMany(mappedBy = "storeOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
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
    public Member(String memberId, String password, String memberName, String nickName, String email, LocalDate birthDate, Role role, Address address, String phoneNum, boolean enabled, String shopIntroduction, String profileImageUrl      ) {
        this.memberId = memberId;
        this.password = password;
        this.memberName = memberName;
        this.nickName = nickName;
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
