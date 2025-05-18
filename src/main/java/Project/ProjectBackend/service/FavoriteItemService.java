package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.FavoriteItemListDto;
import Project.ProjectBackend.entity.FavoriteItem;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.repository.FavoriteRepository;
import Project.ProjectBackend.repository.ItemRepository;
import Project.ProjectBackend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class FavoriteItemService {


    private final FavoriteRepository favoriteRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;

    // 찜한 상품에 추가
    @Transactional
    public void addFavorite(String memberId, Long itemId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        if (favoriteRepository.existsByMemberAndItem(member, item)) {
            throw new IllegalStateException("이미 관심 상품으로 등록되었습니다.");
        }

        // favorite 저장
        FavoriteItem favoriteItem = new FavoriteItem(member, item);
        favoriteRepository.save(favoriteItem);

        // Item의 favoriteCount 증가
        item.increaseFavoriteCount();
        itemRepository.save(item);
    }


    // 찜한 상품에서 삭제
    @Transactional
    public void removeFavorite(String memberId, Long itemId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        // favorite 에서 삭제
        FavoriteItem favoriteItem = favoriteRepository.findByMemberAndItem(member, item)
                .orElseThrow(() -> new IllegalArgumentException("등록된 관심 상품이 없습니다."));
        favoriteRepository.delete(favoriteItem);

        // Item의 favoriteCount 감소
        item.decreaseFavoriteCount();
        itemRepository.save(item);
    }


    // 특정 사용자가 찜한 상품인지 확인
    @Transactional(readOnly = true)
    public boolean isFavorite(String memberId, Long itemId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        return favoriteRepository.existsByMemberAndItem(member, item);
    }


    // 찜 된 횟수 카운트
    public long countFavoritesByItemId(Long itemId) {
        return favoriteRepository.countByItem_ItemId(itemId);
    }

    // 특정 사용자가 찜한 상품 목록 조회
    @Transactional(readOnly = true)
    public Slice<FavoriteItemListDto> getFavoriteItemsByMember(String memberId, Pageable pageable) {
        // 🔹 memberId를 가지고 Member 엔티티를 먼저 조회
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 🔹 Member 객체를 사용하여 쿼리 실행
        return favoriteRepository.findByMember(member, pageable)
                .map(FavoriteItemListDto::from);
    }
}
