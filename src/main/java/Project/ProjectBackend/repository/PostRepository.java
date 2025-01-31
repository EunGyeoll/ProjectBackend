package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Slice<Post> findByWriter_MemberId(String memberId, Pageable pageable);
    Slice<Post> findByWriter_MemberIdOrderByPostDateDesc(String memberId, Pageable pageable);

}
