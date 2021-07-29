package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.SimpleOrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor

/**
 * 이거는 일대다 최적화.
 */
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    //v1
    //일단 여기도 엔티티를 직접 노출시기면 좋지않다.
    // 정리 1.
    //   1-1 . 엔티티 이름을 바꾸면 api스펙이 바뀌어 버리기 떄문에 다른 사람이 api를 쓰려면 일반 dto를 까기보다는
    //         엔티티, api스펙 둘다 문서를 열어봐야 알 수 있다는 단점이 존재.

    //   1-2 . LAZYLOADING ISSUE가 존재할 수 있음.
    //   1-3 . 엔티티를 직접 노출하면 프록시때문에 JSON으로 만드는 과정중에 문제가 생겨 트레이스 로딩이 길게됨.


    /*
     * 제일 성능쓰레기 api
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress(); //이 두줄은 강제초기화
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                orderItem.getItem().getName(); //->객체그래프를 쭈욱 초기화.
            }
            //orderItems.stream().forEach(o->o.getItem().getName());
        }
        return all;
        /*
         * 빈 추가 수동으로 해준거에 하이버네이트5모듈 때문에 프록시객체면 출력안함 ㄱㅇㄷ
         *
         * 그래도 결국 엔티티를 직접 노출하기 때문에 존나 쓰레기 같은 방식임
         */
    }


//v2

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return collect;
    }


    @Getter
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order o) {
            orderId = o.getId();
            name = o.getMember().getName();
            orderDate = o.getOrderDate();
            orderStatus = o.getStatus();
            address = o.getDelivery().getAddress();
//            o.getOrderItems().stream().forEach(order-> order.getItem().getName());
            orderItems = o.getOrderItems()
                    .stream()
                    .map(orderItem->new OrderItemDto(orderItem))
                    .collect(toList());


            /**
             *1.
             * orderItems = o.getOrderItems();
             * 이것만 적었을 때는 items의 item들이 초기화가 되지 않아서서 하이버네이트5모듈에 의해서
             * null 값이 적용되어진다.
             *
             * 그래서 프록시를 초기화해주어야 뜨게 된다.
             *
             *
             * 2.
             * o.getOrderItems().stream().forEach(order-> order.getItem().getName());
             * 이런식으로 프록시 객체 초기화
             *
             *
             * 3.
             * 하지만 item자체도 dto로 반환해야한다.
             *
             * 문제점은 쿼리가 존나많이나가가             * */
        }
        @Getter
        private class OrderItemDto {

            private String itemName;
            private int orderPrice;
            private int count;
            public OrderItemDto(OrderItem orderItem) {
                itemName = orderItem.getItem().getName();
                orderPrice = orderItem.getItem().getPrice();
                count = orderItem.getCount();
            }
        }
    }


    //v3
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        /*
         * 데이터가 뻥튀기 된다는 단점이 있다.
         */
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        //일단 이것도 dto로 보냄.
        return collect;
        /**
         * 뻥튀기가 되어져서 제이슨이 나온다.
         */

        /*
        *return em.createQuery("select distinct o from Order o" +
                " join fetch o.member m" +
                " join fetch o.delivery d" +
                " join fetch o.orderItems oi" +
                " join fetch oi.item i", Order.class).getResultList();
            }
            * distinct -> 해주면 실제 db에는 쿼리가 distinct로 나가나 관계없고
            * jpa자체에서order와 아이디 값이 동일한 객체를 버린다.
            *
            * 페이징 절대하지마라 ( 메모리에 퍼올려서 페이징하기 때문에 distinct 없는 결과랑 같음. )
            * 컬렉션두개이상 페이징하지마라쓰바러마마
         */
    }





    //v3-1
    //페이징할 수 있어! 어떻게할까????????
    //원래 페이징 못함. 왜냐 db에서 다퍼올려서 메모리에서 페이징하기때문에 distinct없는거랑 똑같음.
    //컬렉션을 페치조인하게 되면 다쪽이 기준이 되어버린다.
    //최악의 경우 이상한 예측불가능 값이 나온다.

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
        /**
         *  1. 해결방안 일단 xtoOne관계를 다 죄다 모두다 페치조인을 쭈욱 걸어준다.
         *      그러다가 컬렉션(다)가 나올때까지 쭈욱걸어주고 다가 나오면 멈춘다.
         *  2. 그리고 컬렉션자체를 지연로딩으로 셋팅한다.
         *     어짜피 처음부터 지연로딩으로 세팅하고 시작하자.
         *
         *  3.그리고 dto로 옮겨서 다꺼내주면 컬렉션은 지연로딩으로 프록시 초기화햊주면
         *    (딜리버리 멤버)(페치조인)1번 + 오더아이템스(1번) + 아이템(2)2번
         *                               오더아이템스(1번) + 아이템(2)2번
         *                               총 7번 쿼리
         *  4. 이걸 없애기위해서
         *      jpa:properties:hibernate:default_batch_fetch_size : 100
         *      배치사이즈 @batchsize 둘중에하나 적용.
         *
         *      그럼 페치조인 빼고 2번의 쿼리가 한번에 불러와짐.
         *      그리고 나머지 4번의 아이템 쿼리가 한번에 불러와짐.
         *
         *      그럼 총 페치조인 포함해서 7번이아닌 3번으로 반을 줄일 수 있다.
         *
         *
         */
    }





    //v4
    //주문 조회 V4: JPA에서 DTO 직접 조회
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();
       //클래스 안에 설명
    }

    //v5
    //주문 조회 V5: JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }

    //v6
    //주문 조회 V6: JPA에서 DTO로 직접 조회, 플랫 데이터 최적화
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6(){
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();


        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())))
                .entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
        /*
        단점
        쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가
        추가되므로 상황에 따라 V5 보다 더 느릴 수 도 있다.
            => 데이터가 작으면 왠만하면 이렇게 하는것도 괜찮을 수 있다.
        애플리케이션에서 추가 작업이 크다.

        페이징 불가능
            => 1: 1
               1: 2
               2: 3
               2: 4
               행마다 제이슨으로 뿌려주기 떄문에 중복이 있어서 발라내는 작업이 위에 return 그룹핑을 해서 해줄 수 있다.
               이코드는 나중에 니가 분석해봐라!

         */
    }
}
