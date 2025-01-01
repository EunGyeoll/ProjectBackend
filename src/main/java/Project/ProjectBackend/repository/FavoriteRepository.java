package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Favorite;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByMemberAndItem(Member member, Item item);
    Optional<Favorite> findByMemberAndItem(Member member, Item item);
    List<Favorite> findByMember(Member member);

//     boolean existsByMemberIdAndItemId(String memberId, Long itemId);
//        void deleteByMemberIdAndItemId(String memberId, Long itemId);
//        List<Favorite> findByMemberId(String memberId);

    }

