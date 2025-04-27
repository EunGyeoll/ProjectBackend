package Project.ProjectBackend.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SortService {

    public Sort createSort(String sortOption, String entityType) {
        switch (entityType.toLowerCase()) {
            case "item":
                return getItemSort(sortOption);
            case "post":
                return getPostSort(sortOption);
            case "favoriteItem":
                return Sort.by(Sort.Direction.DESC, "createdAt"); // 찜한 상품 최신순
            case "likedPost":
                return Sort.by(Sort.Direction.DESC, "createdAt"); // 좋아요한 게시글 최신순
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt"); // 기본 정렬
        }
    }

    private Sort getItemSort(String sortOption) {
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
    }

    private Sort getPostSort(String sortOption) {
        switch (sortOption.toLowerCase()) {
            case "mostHitCount":
                return Sort.by(Sort.Direction.DESC, "hitCount");
            case "mostlikeCount":
                return Sort.by(Sort.Direction.DESC, "likeCount");
            case "latest":
            default:
                return Sort.by(Sort.Direction.DESC, "postDate"); // 최신 게시글 정렬
        }
    }
}
