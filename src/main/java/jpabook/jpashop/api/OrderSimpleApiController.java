package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.SimpleOrderQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (x to One)의 관계
 * Order
 * Order - > member ( manyToONE)
 * Order - > Delivery (One TO ONE)
 *
 *  성능최적화하기
 *
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;


    //----------------V1
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        return all;
        //생자로 넘기면 검색조건이 없어서 그냥 불러온다.
        /*
         * 첫번째 문제
         * 이렇게 리턴시키면 어떻게될까?
         *
         * -> order를 불러오면서 order와 연관된 member로 간다
         * -> member에서 양방향이 걸려있는 orders를 불러온다.
         * -> 그러면 또 orders로 가면서 양방향으로 아주 엔티티끼리 지랄을 해서 끝도없이 trace가 나온다.
         *
         * @JsonIgnore 이걸로 one쪽에 양방향중 어느 한쪽은 jackson관계를 끊어주어야 한다.
         *
         * -하지만! 다음문제!
         * 두번째 문제
         *
         * !!!!!!!!!!!해결방법 하이버네이트 5 모듈 라이브러리 다운로드 받기기
         * 1. implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5' 그라들에 등록

        *  2. @Bean
                public Hibernate5Module hibernate5Module(){
                    return new Hibernate5Module();
                }
                * 빈등록 꼭해주기.
                *
                *
                *
                * 하지만 이방법은 무의미하다 왜냐?
                * 엔티티를 밖으로 외부로 노출하기때문이다!
                *
                * 강제초기화는 .getname등을 서서 임의로 레이지로딩을 로딩해줍니다.
        * */


        //	하이버네이트 5 모듈 다운로드
        //	왜냐 ? 전체 주문정보를 다불러오면 order클래스에 붙어있던 xtoOne의 아이들이 프록시객체로 지연로딩하는데
        //	스프링은 그 프록시객체를 처리할 줄 모른다. 그래서 무한루프생기면서 무한 트레이스가 보이게되는거
        //	그러다보니까 스프링은 자꾸 트레이스걸면서 패킷도 정상적으로 못받음
        //	결론은 프록시가 문제
    }



    //----------------V2



    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> collect = orders.stream().map(o -> new SimpleOrderDto(o)).collect(Collectors.toList());
        return collect;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        //dto 가 중요한 엔티티에 의존해도 괜찮아! 상관없어
        //별로 중요하지 않은 곳에서 중요한 것을 의존하는거라서 ㄱㅊ
        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();        //여기서 lazy 초기화하고있어1 쿼리나감
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();        //여기서 lazy 초기화하고있어2 쿼리나감

            //1+N+N문제가 발생
            //왜냐
            // MEMBER호출시에 USERA,USERB에 대한 정보2개 멤버끍어오는 쿼리 2개
            // MEMBER호출시에 USERA,USERB에 대한 각각 주문정보 2개 디리버리 끍어오는 쿼리 2개

            // 하지만 지연로딩도 USERA하나가 주문을 두개씩 했으면 영속성컨텍스를 조회하기 때문에 USERA는 일단 쿼리날려서 끍어오고
            // 두번쨰로 영속성컨텍스트에 USERA가 있는 상태라 그거 또 이용함. 그럴때는 쿼리가 한번 줄어들어어
        }
   }





    //----------------V3

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();//fetch join gogo
        List<SimpleOrderDto> result = orders.stream().map(o -> new SimpleOrderDto(o)).collect(Collectors.toList());

        return result;
    }


    //----------------V4
    //이번엔 내가 dto로 안감싸고 걍 바로 dto로 바꿔주는거 ㄱㄱㄱㄱㄱㄱ
    @GetMapping("/api/v4/simple-orders")
    public List<SimpleOrderQueryDto> ordersV4(){
        return orderRepository.findOrderDtos();
        /*
         * new 오퍼레이션으로 만들어주게 되면 성능상차이는 없지만 쿼리줄을 더 줄일수 있다.
         * 그래서 왠만하면 패치조인으로 가져오자 new 말구
         *
         * new 로 dto를 api스펙상 repository에 종속되게 생성한 쿼리들은 따로 repository를 만들어서 갖다가 저장하고 쓴다.
         */
        /**
         * public List<SimpleOrderQueryDto> findOrderDtos() {
         *         List<SimpleOrderQueryDto> resultList = em.createQuery("select new jpabook.jpashop.repository.SimpleOrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
         *                         "from Order o " +
         *                         "join o.member m " +
         *                         "join o.delivery d"
         *                 , SimpleOrderQueryDto.class)
         *                 .getResultList();
         *         return resultList;
         */
    }

}
