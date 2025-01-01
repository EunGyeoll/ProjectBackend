        package Project.ProjectBackend.service;

        import Project.ProjectBackend.dto.ItemResponseDto;
        import Project.ProjectBackend.entity.Favorite;
        import Project.ProjectBackend.entity.Item;
        import Project.ProjectBackend.entity.Member;
        import Project.ProjectBackend.repository.FavoriteRepository;
        import Project.ProjectBackend.repository.ItemRepository;
        import Project.ProjectBackend.repository.MemberRepository;
        import lombok.RequiredArgsConstructor;
        import org.springframework.stereotype.Service;

        import java.util.List;
        import java.util.stream.Collectors;

        @Service
        @RequiredArgsConstructor
        public class FavoriteService {

            private final FavoriteRepository favoriteRepository;
            private final ItemRepository itemRepository;
            private final MemberRepository memberRepository;

            // 찜한 상품에 추가
            public void addFavorite(String memberId, Long itemId) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new IllegalArgumentException("Member not found"));
                Item item = itemRepository.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException("Item not found"));

                if (favoriteRepository.existsByMemberAndItem(member, item)) {
                    throw new IllegalStateException("이미 관심 상품으로 등록되었습니다.");
                }

                Favorite favorite = Favorite.builder()
                        .member(member)
                        .item(item)
                        .build();
                favoriteRepository.save(favorite);
            }

            // 찜한 상품에서 삭제
            public void removeFavorite(String memberId, Long itemId) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new IllegalArgumentException("Member not found"));
                Item item = itemRepository.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException("Item not found"));

                Favorite favorite = favoriteRepository.findByMemberAndItem(member, item)
                        .orElseThrow(() -> new IllegalArgumentException("등록된 관심 상품이 없습니다."));
                favoriteRepository.delete(favorite);
            }

            // 특정 사용자가 찜한 상품인지 확인
            public boolean isFavorite(String memberId, Long itemId) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new IllegalArgumentException("Member not found"));
                Item item = itemRepository.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException("Item not found"));

                return favoriteRepository.existsByMemberAndItem(member, item);
            }

            // 특정 사용자가 찜한 상품 목록
            public List<ItemResponseDto> getFavorites(String memberId) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new IllegalArgumentException("Member not found"));

                List<Favorite> favorites = favoriteRepository.findByMember(member);
                return favorites.stream()
                        .map(fav -> ItemResponseDto.from(fav.getItem()))
                        .collect(Collectors.toList());
            }
        }

