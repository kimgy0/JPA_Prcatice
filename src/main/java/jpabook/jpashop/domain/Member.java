package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded private Address address;


    //조회 v1 api 설명
    /*
     * @JsonIgnore 이라는 어노테이션을 달아주게 되면
     * 엔티티를 외부로 던질때 해당 orders에 대한 정보를 제이슨으로 넘겨주지 않는다.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();
}
