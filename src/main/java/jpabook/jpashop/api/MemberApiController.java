package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.DenyAll;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    //v1
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * {
     *     "name" : "hello"
     * }
     * 패킷에 이 url로 던져라 제이슨으로!
     */
    /*
     * v1의 문제는 엔티티에서 name 변수 값을 username으로 변환했다고 치면
     * api의 스펙이 그냥 바뀌어버린다.
     * 엔티티를 손대서 api의 스펙이 바뀌어버린다는 것은 굉장히 좋지 않다.
     *
     * 다음부터는 엔티티를 절대 파라미터로 받지마라.
     */
    //@RequestBody는 패킷의 바디에 제이슨을 객체로 풀어줌


    //v2
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * dto를 파라미터로 받아서 엔티티에 세팅해줌.
     * 엔티티가 변경이 되어도 api스펙이 변경된다.
     * 실문에서 엔티티를 파라미터로 받지마세요.
     *
     * api사용자가 까볼때 api스펙문서를 까보지 않는이상 모름
     * 그래서 dto를 받으면 어던 인자가 requestbody라고 하는지??
     */

    //v2-1
        //회원수정 api

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request){

        memberService.update(id,request.getName());
        //업데이트할 땐 변경감지를 꼭사용해라!
        //merge 는 null 값문제 때문에 다 대체하기 떄문에 안된다!
        /* update 메서드에서 member를 바로 가져와도 되는데 이거는 너무 쿼리분리성이 없음 : 참고 8분 수정api*/
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());

        /**
         * {
         *     "id" : 1,
         *     "name" : "hello"
         * }
         * put으로 넘겨줍니다.
         * 패킷에 이 url로 던져라 제이슨으로!
         */
    }


    //v3
    //회원 조회 api (get)
    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        return memberService.findMembers();
        /*
         * 멤버클래스 엔티티에
         * @JsonIgnore 이라는 어노테이션을 달아주게 되면
         * 엔티티를 외부로 던질때 해당 orders에 대한 정보를 제이슨으로 넘겨주지 않는다.
         *
         * +로 dto를 반환해야하는데 엔티티를 외부로 직접 던져주니까 에바임
         *
         * +이렇게 개발하면 엔티티를 건드려야하는데 엔티티가 자꾸 변하니까 에바임
         * 이런 api스펙이 변하면 좆같음.
         */
    }

    //v3-1
    @GetMapping("/api/v2/members")
    public Result membersV2(){
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream().map(m -> new MemberDto(m.getName())).collect(Collectors.toList());

        return new Result(collect);
    }
    //--------------------------------------------------------------------------------------------------------------
    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }


    //--------------------------------------------------------------------------------------------------------------
    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
    //--------------------------------------------------------------------------------------------------------------
    @Data
    static class CreateMemberRequest {
        private String name;
    }
    //--------------------------------------------------------
    @Data
    static class UpdateMemberRequest {
        @NotEmpty
        private String name;
    }
    // DTO는 막 세터 게터 생성자 다써주고
    // 엔티티는 최소 객체만.
    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }
}
