package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.FavoriteItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class FavoriteItemListDto {
    private Long favoriteId;
    private Long itemId;
    private String itemName;
    private Integer price;
    private String representativeImagePath;
    private LocalDateTime createdAt;

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static FavoriteItemListDto from(FavoriteItem favoriteItem) {
        return FavoriteItemListDto.builder()
                .favoriteId(favoriteItem.getFavoriteId())
                .itemId(favoriteItem.getItem().getItemId())
                .itemName(favoriteItem.getItem().getItemName())
                .price(favoriteItem.getItem().getPrice())
                .representativeImagePath(favoriteItem.getRepresentativeImagePath())
                .createdAt(favoriteItem.getCreatedAt())
                .build();
    }
}
