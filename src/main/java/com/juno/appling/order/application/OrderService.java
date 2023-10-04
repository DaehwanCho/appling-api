package com.juno.appling.order.application;

import com.juno.appling.global.util.MemberUtil;
import com.juno.appling.member.domain.Member;
import com.juno.appling.member.domain.Seller;
import com.juno.appling.order.domain.*;
import com.juno.appling.order.dto.request.CompleteOrderRequest;
import com.juno.appling.order.dto.request.TempOrderDto;
import com.juno.appling.order.dto.request.TempOrderRequest;
import com.juno.appling.order.dto.response.CompleteOrderResponse;
import com.juno.appling.order.dto.response.OrderListResponse;
import com.juno.appling.order.dto.response.OrderVo;
import com.juno.appling.order.dto.response.PostTempOrderResponse;
import com.juno.appling.order.dto.response.TempOrderResponse;
import com.juno.appling.order.enums.OrderStatus;
import com.juno.appling.product.domain.Product;
import com.juno.appling.product.domain.ProductRepository;
import com.juno.appling.product.enums.Status;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderCustomRepository orderCustomRepository;
    private final MemberUtil memberUtil;

    @Transactional
    public PostTempOrderResponse postTempOrder(TempOrderRequest tempOrderRequest, HttpServletRequest request){
        Member member = memberUtil.getMember(request);

        /**
         * 1. 주문 발급
         * 2. 주문 상품 등록
         * 3. 판매자 리스트 등록
         */

        // 주문 발급
        StringBuffer sb = new StringBuffer();
        List<TempOrderDto> requestOrderProductList = tempOrderRequest.getOrderList();
        List<Long> requestProductIdList = requestOrderProductList.stream().mapToLong(o -> o.getProductId())
                .boxed().collect(Collectors.toList());

        List<Product> productList = productRepository.findAllById(requestProductIdList);
        productList = productList.stream().sorted((p1, p2) -> p2.getPrice() - p1.getPrice()).collect(Collectors.toList());

        if((requestOrderProductList.size() != productList.size()) || productList.size() == 0){
            throw new IllegalArgumentException("유효하지 않은 상품이 존재합니다.");
        }

        sb.append(productList.get(0).getMainTitle());
        if(productList.size() > 1){
            sb.append(" 외 ");
            sb.append(productList.size() -1);
            sb.append("개");
        }
        Order saveOrder = orderRepository.save(Order.of(member, sb.toString()));

        // 주문 상품 등록
        Map<Long, Integer> eaMap = new HashMap<>();
        for(TempOrderDto o : requestOrderProductList){
            eaMap.put(o.getProductId(), o.getEa());
        }

        for(Product p : productList){
            if(p.getStatus() != Status.NORMAL){
                throw new IllegalArgumentException("상품 상태가 유효하지 않습니다.");
            }

            int ea = eaMap.get(p.getId());
            OrderItem orderItem = orderItemRepository.save(OrderItem.of(saveOrder, p, ea));
            saveOrder.getOrderItemList().add(orderItem);
        }

        return new PostTempOrderResponse(saveOrder.getId());
    }

    public TempOrderResponse getTempOrder(Long orderId, HttpServletRequest request){
        /**
         * order id와 member 정보로 임시 정보를 불러옴
         */
        Order order = checkOrder(request, orderId);

        return TempOrderResponse.of(order);
    }

    @Transactional
    public CompleteOrderResponse completeOrder(CompleteOrderRequest completeOrderRequest, HttpServletRequest request){
        /**
         * 주문 정보를 update 해야됨!
         * 1. 주문 상태 변경
         * 2. 주문자, 수령자 정보 등록
         * 3. 주문 번호 만들기
         */
        Long orderId = completeOrderRequest.getOrderId();
        Order order = checkOrder(request, orderId);

        List<OrderItem> orderItemList = order.getOrderItemList();
        for(OrderItem oi : orderItemList){
            deliveryRepository.save(Delivery.of(order, oi, completeOrderRequest));
        }

        order.statusComplete();

        LocalDateTime createdAt = order.getCreatedAt();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String orderNumber = String.format("ORDER-%s-%s", createdAt.format(formatter), orderId);
        order.orderNumber(orderNumber);

        return new CompleteOrderResponse(orderId, orderNumber);
    }

    private Order checkOrder(HttpServletRequest request, Long orderId) {
        Member member = memberUtil.getMember(request);
        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                new IllegalArgumentException("유효하지 않은 주문 번호입니다.")
        );

        if(member.getId() != order.getMember().getId()) {
            log.info("[getOrder] 유저가 주문한 번호가 아님! 요청한 user_id = {} , order_id = {}", member.getId(), order.getId());
            throw new IllegalArgumentException("유효하지 않은 주문입니다.");
        }

        if(order.getStatus() != OrderStatus.TEMP) {
            throw new IllegalArgumentException("유효하지 않은 주문입니다.");
        }

        return order;
    }

    public OrderListResponse getOrderBySeller(Pageable pageable, String search, OrderStatus orderStatus, HttpServletRequest request) {
        Seller seller = memberUtil.getSeller(request);
        Page<OrderVo> page = orderCustomRepository.findAllBySeller(pageable, search,
            orderStatus, seller);

        return new OrderListResponse(page.getTotalPages(), page.getTotalElements(),
            page.getNumberOfElements(), page.isLast(), page.isEmpty(), page.getContent());
    }
}
