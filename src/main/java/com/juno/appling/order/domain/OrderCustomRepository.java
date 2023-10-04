package com.juno.appling.order.domain;

import com.juno.appling.global.querydsl.QuerydslConfig;
import com.juno.appling.member.domain.Seller;
import com.juno.appling.order.dto.response.OrderVo;
import com.juno.appling.order.enums.OrderStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderCustomRepository {
    private final QuerydslConfig q;

    public Page<OrderVo> findAllBySeller(Pageable pageable, String search, OrderStatus orderStatus, Seller seller) {
        QOrder order = QOrder.order;

        BooleanBuilder builder = new BooleanBuilder();

        search = Optional.ofNullable(search).orElse("").trim();
        if (!search.equals("")) {
            builder.and(order.orderItemList.any().product.mainTitle.contains(search));
        }
        if (orderStatus != null) {
            builder.and(order.status.eq(orderStatus));
        }

        builder.andNot(order.status.eq(OrderStatus.TEMP));
        builder.and(order.orderItemList.any().product.seller.eq(seller));

        List<OrderVo> content = q.query()
            .select(Projections.constructor(OrderVo.class, order))
            .from(order)
            .where(builder)
            .orderBy(order.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        Long total = q.query().from(order).where(builder).stream().count();
        return new PageImpl<>(content, pageable, total);
    }

}
