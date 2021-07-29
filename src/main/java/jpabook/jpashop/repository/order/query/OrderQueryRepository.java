package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;


    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrdeItems(o.getOrderId());
            o.setOrderItem(orderItems);
        });
        return result;
    }

    //v4
    //주문 조회 V5: JPA에서 DTO 직접 조회
    private List<OrderItemQueryDto> findOrdeItems(Long orderId) {
        return em.createQuery("select" +
                " new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }
    /*
     * 페치조인을 컬렉션에 대고 하면 다쪽이 기준이 된다. 그래서 객체로 만들때 일반 조인으로 dto조회를
     * 하는데 orderItem을 기준으로 하고 item을 바라보면 xtoOne관계가 되니까 데이터가 뻥튀기가 될 일이 없다.
     */

    private List<OrderQueryDto> findOrders() {
        return em.createQuery("select new " +
                "jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    /**
     * 두 메서드 서로 둘다 xto One 쪽을 바라보게하여 객체로 만들어냅니다.
     * new 메서드를 사용하면서 만듬.
     *
     * 그래서 총 order 한번, 그리고 orderitem에서 order의 id값을 조건으로 조인했으니까 order 1,2번 items 쿼리 한개씩.
     * 하지만 이것도 어짜피 n+1문제를 야기함. 어째든 오더 하나에 오더아이템 찾으러 오더갯수만큼 +됨.
     */







    //v5
    //주문 조회 V5: JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화
    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders(); //일단 이거는 findorders 가져오는거까지는 v4랑 다른게 없음.
        // v4는 루프를 돌면서 dto를 만들었는데 이번엔 한번에 가져온다.

        List<Long> orderIds = result
                                .stream()
                                .map(o -> o.getOrderId())
                                .collect(Collectors.toList()); //id 추출

        List<OrderItemQueryDto> orderItmes = em.createQuery("select" +
                        " new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id in :orderIds" //이번엔 루프를 돌면서 dto를 안가져오고 in절에 ids를 인쿼리로 한번에 불러올겨
                , OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap
                = orderItmes
                .stream()
                .collect(Collectors
                        .groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));//맵으로 그루핑해서 ㄱㄱ

        result.forEach(o->o.setOrderItem(orderItemMap.get(o.getOrderId())));

        return result; //이렇게 하면 쿼리 두번으로 최적화가 가능합니다.


    }









    //v6
    //주문 조회 V6: JPA에서 DTO로 직접 조회, 플랫 데이터 최적화
    //v5는 쿼리두번에 가져오는 반면 이거는 한번에 다끍어옴.
    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                "select " +
                        "new jpabook.jpashop.repository.order.query.OrderFlatDto" +
                        "(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }
}
