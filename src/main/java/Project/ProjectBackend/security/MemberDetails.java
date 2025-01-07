//package Project.ProjectBackend.security;
//
//import java.util.List;
//
//import Project.ProjectBackend.entity.Member;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//
//public class MemberDetails extends User {
//    private Member member;
//
//    public MemberDetails(Member member, List<GrantedAuthority> authorities) {
//        super(member.getMemberId(),
//                member.getPassword(),
//                member.getEnabled(),
//                true, true, true,
//                authorities);
//        this.member = member;
//    }
//
//    public Member getMember() {
//        return member;
//    }
//}
