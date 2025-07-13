package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikedPostRepository extends JpaRepository<LikedPost, Long> {

//    @Modifying
//    @Query("DELETE FROM LikedPost lp where lp.post = :post")
//    void deleteByPostId(@Param("postId")Long postId);

    void deleteAllByPost_PostId(Long postId);

    boolean existsByMemberAndPost(Member member, Post post);

    Optional<LikedPost> findByMemberAndPost(Member member, Post post);

    Slice<LikedPost> findByMember(Member member, Pageable pageable);

    long countByPost_PostId(long postId);
}
