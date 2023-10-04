package com.juno.appling.order.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.juno.appling.order.domain.Order;
import com.juno.appling.order.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderVo {
    private Long orderId;
    private String orderNumber;
    private String orderName;
    private List<OrderItemVo> orderItemList;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public OrderVo(Order order) {
        this.orderId = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.orderName = order.getOrderName();
        this.status = order.getStatus();
        this.orderItemList = order.getOrderItemList().stream().map(oi -> OrderItemVo.of(oi)).collect(Collectors.toList());
        this.createdAt = order.getCreatedAt();
        this.modifiedAt = order.getModifiedAt();
    }
}
