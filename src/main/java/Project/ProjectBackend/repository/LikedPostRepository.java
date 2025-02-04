package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikedPostRepository extends JpaRepository<LikedPost, Long> {

    boolean existsByMemberAndPost(Member member, Post post);
    Optional<LikedPost> findByMemberAndPost(Member member, Post post);
    Slice<LikedPost> findByMember(Member member, Pageable pageable);

}
