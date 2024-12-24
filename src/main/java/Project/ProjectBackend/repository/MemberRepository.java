package Project.ProjectBackend.repository;

import Project.ProjectBackend.domain.Member;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByEmail(String email);

//    public List<Member> findByName(String name) {
//        return em.createQuery("select m from Member m where m.name = :name", Member.class)
//                .setParameter("name", name)
//                .getResultList();
//    }
}
