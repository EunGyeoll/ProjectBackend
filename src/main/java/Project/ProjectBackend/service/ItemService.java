package Project.ProjectBackend.service;

import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.dto.ItemRequestDto;
import Project.ProjectBackend.repository.ItemRepository;
import Project.ProjectBackend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;

    // 모든 아이템 조회
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // 특정 판매자가 등록한 아이템 조회
    public List<Item> getItemsBySeller(String sellerId) {
        Member seller = memberRepository.findByMemberId(sellerId)
                .orElseThrow(()-> new IllegalArgumentException("판매자를 찾을 수 없습니다."));
        return itemRepository.findBySeller(seller);
    }

    // 아이템 등록
    public Item createItem(ItemRequestDto itemRequestDto) {
        // 작성자(Member) 조회
        Member seller = memberRepository.findById(itemRequestDto.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("판매자가 존재하지 않습니다."));

        // Item 엔티티 생성 (Builder 사용)
        Item item = Item.builder()
                .seller(seller)
                .itemName(itemRequestDto.getItemName())
                .price(itemRequestDto.getPrice())
                .description(itemRequestDto.getDescription())
                .stockQuantity(itemRequestDto.getStockQuantity())
                .build();

        // 데이터베이스에 저장
        return itemRepository.save(item);
    }



    // 아이템 수정
    public Item updateItem(Long itemId, ItemRequestDto updatedItemDto) {
        // 기존 아이템 조회
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));

        // 기존 아이템의 수정 사항을 반영하여 새로운 아이템 생성
        Item updatedItem = Item.builder()
                .itemId(existingItem.getItemId()) // 기존 ID 유지
                .seller(existingItem.getSeller()) // 판매자 정보 유지
                .itemName(updatedItemDto.getItemName())
                .price(updatedItemDto.getPrice())
                .description(updatedItemDto.getDescription())
                .stockQuantity(updatedItemDto.getStockQuantity())
//                .category(updatedItemDto.getCategory())
                .build();

        // 데이터베이스에 저장
        return itemRepository.save(updatedItem);
    }


    // 아이템 삭제
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));
        itemRepository.delete(item);
    }
}
