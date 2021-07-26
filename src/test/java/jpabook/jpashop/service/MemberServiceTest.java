package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) // 완전히 스프링이랑 합쳐서 테스트
@SpringBootTest
@Transactional // 이게 있으면 기본적으로 test에서는 롤백을 해버린다.
               // 롤백이 되지 않게 하는 방법은 테스트 메서드 위에 @Rollback(false)
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    // @Rollback(false) 롤백이 되지 않게 하는 방법은 테스트 메서드 위에 @Rollback(false)
    public void 회원가입() throws Exception{
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long saveId = memberService.join(member);

        //then
        assertEquals(member, memberRepository.findOne(saveId));

    }
    @Test(expected = IllegalStateException.class)
    //(expected = IllegalStateException.class) 이 예외가 생기게 되면 통과시킵니다.
    public void 중복_회원_예약() throws Exception{
        //given
        Member member1 = new Member();
        member1.setName("member1");

        Member member2 = new Member();
        member2.setName("member2");
        //when
        memberService.join(member1);
        try{
            memberService.join(member2);
        }catch (IllegalStateException e){
            return;
        }

        //then
        Assert.fail("예외가 발생한다!");

    }

}