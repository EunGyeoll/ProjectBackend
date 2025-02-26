package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class MemberMyPageDto {
    private String memberId;
    private String name;
    private String email;
    private Role role;
    private boolean enabled;
    private LocalDate birthDate;
    private String phoneNum;
    private AddressDto address;
    private List<ItemListDto> items;
    private boolean itemsHasNext;
    private List<PostListDto> posts;
    private boolean postsHasNext;
    private List<FavoriteItemListDto> favoriteItems;
    private boolean favoriteItemsHasNext;
    private List<LikedPostListDto> likedPosts;
    private boolean likedPostsHasNext;
    private boolean isOwnProfile;
    private String shopIntroduction;
    private String profileImageUrl;

    public static MemberMyPageDto from(
            Member member,
            boolean isOwnProfile,
            List<ItemListDto> items,
            boolean itemsHasNext,
            List<PostListDto> posts,
            boolean postsHasNext,
            List<FavoriteItemListDto> favoriteItems,
            boolean favoriteItemsHasNext,
            List<LikedPostListDto> likedPosts,
            boolean likedPostsHasNext
    ) {
        return MemberMyPageDto.builder()
                .memberId(member.getMemberId())
                .name(isOwnProfile ? member.getName() : null)
                .email(isOwnProfile ? member.getEmail() : null)
                .role(member.getRole())
                .enabled(member.isEnabled())
                .birthDate(isOwnProfile ? member.getBirthDate() : null)
                .phoneNum(isOwnProfile ? member.getPhoneNum() : null)
                .address(isOwnProfile ? AddressDto.from(member.getAddress()) : null)
                .items(items)
                .itemsHasNext(itemsHasNext)
                .posts(posts)
                .postsHasNext(postsHasNext)
                .favoriteItems(favoriteItems)
                .favoriteItemsHasNext(favoriteItemsHasNext)
                .likedPosts(likedPosts)
                .likedPostsHasNext(likedPostsHasNext)
                .isOwnProfile(isOwnProfile)
                .shopIntroduction(member.getShopIntroduction())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }
}
