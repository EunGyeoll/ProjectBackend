package Project.ProjectBackend.service;

import Project.ProjectBackend.domain.Post;
import Project.ProjectBackend.domain.Member;
import Project.ProjectBackend.dto.PostRequestDto;
import Project.ProjectBackend.dto.PostResponseDto;
import Project.ProjectBackend.repository.PostRepository;
import Project.ProjectBackend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 게시글 등록
//    public void createBoard(BoardRequestDto requestDto) {
//        Board board = Board.builder()
//                .writer(requestDto.getWriter())
//                .title(requestDto.getTitle())
//                .content(requestDto.getContent())
//                .build();
//        boardRepository.save(board);
//    }

    public void createBoard(PostRequestDto requestDto) {
        // 작성자(Member) 조회
        Member writer = memberRepository.findById(requestDto.getWriter())
                .orElseThrow(() -> new IllegalArgumentException("작성자가 존재하지 않습니다."));

        // Board 엔티티 생성
        Post post = Post.builder()
                .writer(writer) // Member 객체 설정
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .build();

        postRepository.save(post); // 게시글 저장
    }


    // 모든 게시글 조회
    public List<PostResponseDto> getAllBoards() {
        return postRepository.findAll().stream()
                .map(PostResponseDto::new) // 엔티티 -> DTO 변환
                .collect(Collectors.toList());
    }

    // 작성자별 게시글 상세 조회
    public PostResponseDto getBoard(Long boardNo) {
        Post post = postRepository.findById(boardNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        return new PostResponseDto(post); // 엔티티를 DTO로 변환하여 반환
    }



    // 게시글 삭제
    public void deleteBoard(Long boardNo) {
        postRepository.deleteById(boardNo);
        // deleteById라는 메소드가 jpa에서 만들어진다고..? 더 알아보기
    }
}
