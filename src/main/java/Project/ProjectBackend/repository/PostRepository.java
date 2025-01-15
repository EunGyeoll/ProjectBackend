package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 특정 사용자로 조회
//    Slice<Post> findByWriter_MemberIdOrderByPostDateDesc(String memberId, Pageable pageable);
    Slice<Post> findByWriter_MemberId(String memberId, Pageable pageable);

}
