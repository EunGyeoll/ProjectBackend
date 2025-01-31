package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.ItemResponseDto;
import Project.ProjectBackend.entity.FavoriteItem;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.repository.FavoriteRepository;
import Project.ProjectBackend.repository.ItemRepository;
import Project.ProjectBackend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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


    // 특정 사용자가 찜한 상품 목록 조회
    @Transactional(readOnly = true)
    public List<ItemResponseDto> getFavorites(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<FavoriteItem> favoriteItems = favoriteRepository.findByMember(member);
        return favoriteItems.stream()
                .map(fav -> ItemResponseDto.from(fav.getItem()))
                .collect(Collectors.toList());
    }
}
