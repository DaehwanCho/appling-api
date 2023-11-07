package com.juno.appling.order.port;

import com.juno.appling.order.domain.entity.OrderEntity;
import com.juno.appling.order.domain.model.Order;
import com.juno.appling.order.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;


    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(OrderEntity.from(order)).toModel();
    }
}
