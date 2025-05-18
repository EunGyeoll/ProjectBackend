package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.FavoriteItem;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteItem, Long> {

    boolean existsByMemberAndItem(Member member, Item item);
    Optional<FavoriteItem> findByMemberAndItem(Member member, Item item);
    Slice<FavoriteItem> findByMember(Member member, Pageable pageable);
    long countByItem_ItemId(Long itemId);

    }

