package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
// 모든 jpa는 기본적으로 하나의 트랜잭션 안에서 이루어져야한다.
//@Transactional(readOnly = true)
/*
 * readonly 옵션을 true로 주게되면 읽기를 할 때 성능을 최적화 해옵니다.
 */
//@RequiredArgsConstructor => final필드만 골라서 의존성 주입
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 회원 가입
    @Transactional
    public Long join(Member member){
        validateDuplicateMember(member);
        log.info("성공2");
        memberRepository.save(member);
        log.info("성공3");
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 전체 조회
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
        //업데이트할 땐 변경감지를 꼭사용해라!
        //merge 는 null 값문제 때문에 다 대체하기 떄문에 안된다!
    }
}
