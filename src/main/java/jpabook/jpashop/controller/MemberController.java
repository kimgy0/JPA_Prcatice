package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public  String createForm(Model model){
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
        //모델 객체에 빈 껍데기라도 들고 갑시다.
    }

    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result){

        //DTO를 따로 사용해줍시다!

        //API를 사용할 때는 외부에 엔티티를 반환하지마라
        //템플릿 엔진에서는 선택적으로 넘겨서 사용해도된다.

        // 바인딩 result가 있으면 오류가 발생시 기존에 컨트롤러에서 튕겨버리다
        // result에 오류결과가 담겨서 하위 코드를 실행하게 된다.

        // @Valid 멤버 폼도 다시 들고 오게 된다.

        if(result.hasErrors()){
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);
        log.info("성공0");
        memberService.join(member);
        log.info("성공1");
        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model){
        model.addAttribute("members", memberService.findMembers());
        return "members/memberList";
    }

}
