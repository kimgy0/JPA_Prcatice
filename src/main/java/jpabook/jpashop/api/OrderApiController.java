package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor

/**
 * 이거는 일대다 최적화.
 */
public class OrderApiController {

    private final OrderRepository orderRepository;

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
}
