package jpabook.jpashop.repository.order.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class OrderItemQueryDto {
    private String itemName;
    @JsonIgnore
    private Long orderId;
    private int orderPrice;
    private int count;

    public OrderItemQueryDto(Long orderId, String itemName, int orderPrice, int count) {
        this.itemName = itemName;
        this.orderId = orderId;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
