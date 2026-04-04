package study.data_jpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {

    // Pageable 기반 findAll(Pageable)은 Spring Data JpaRepository가 제공하는 기능이므로
    // 순수 JPA 학습용 Repository가 아니라 MemberRepository(Spring Data)를 주입받아야 한다.
    private final MemberRepository memberRepository;

    @GetMapping("/members")
    // Pageable 은 "페이지 번호, 크기, 정렬 조건"을 한 객체로 묶어 받는 스프링 표준 파라미터다.
    // 즉 컨트롤러는 page/size/sort 쿼리 파라미터를 직접 파싱하지 않고
    // Pageable 하나만 받아 Repository로 그대로 넘기면 된다.
    public Page<MemberDto> list(@PageableDefault(size=5, sort="username") Pageable pageable) {
        // @PageableDefault 는 사용자가 page/size/sort 를 안 넘겼을 때의 기본값이다.
        // 지금은 "기본 5건, username 정렬"로 학습하기 쉽게 고정해 둔 것이다.
        //
        // Page.map(...) 을 쓰면 content만 DTO로 바꾸고
        // totalElements, totalPages, number 같은 페이징 메타데이터는 그대로 유지된다.
        return memberRepository.findAll(pageable).map(MemberDto::new);
    }
}
