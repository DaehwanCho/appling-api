package com.juno.appling.order.application;

import com.juno.appling.BaseTest;
import com.juno.appling.member.application.MemberAuthService;
import com.juno.appling.member.domain.Member;
import com.juno.appling.member.domain.MemberRepository;
import com.juno.appling.member.domain.Seller;
import com.juno.appling.member.domain.SellerRepository;
import com.juno.appling.member.dto.request.LoginRequest;
import com.juno.appling.member.dto.response.LoginResponse;
import com.juno.appling.order.domain.DeliveryRepository;
import com.juno.appling.order.domain.Order;
import com.juno.appling.order.domain.OrderItem;
import com.juno.appling.order.domain.OrderItemRepository;
import com.juno.appling.order.domain.OrderRepository;
import com.juno.appling.order.dto.request.TempOrderDto;
import com.juno.appling.order.dto.request.TempOrderRequest;
import com.juno.appling.order.dto.response.OrderListResponse;
import com.juno.appling.order.dto.response.OrderVo;
import com.juno.appling.order.dto.response.PostTempOrderResponse;
import com.juno.appling.product.domain.Category;
import com.juno.appling.product.domain.CategoryRepository;
import com.juno.appling.product.domain.Product;
import com.juno.appling.product.domain.ProductRepository;
import com.juno.appling.product.dto.request.ProductRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest
class OrderServiceTest extends BaseTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberAuthService memberAuthService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SellerRepository sellerRepository;


    private MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    @DisplayName("임시 주문 성공")
    @Transactional
    void tempOrder() {
        //given
        LoginRequest loginRequest = new LoginRequest(MEMBER_EMAIL, "password");
        LoginResponse login = memberAuthService.login(loginRequest);
        request.addHeader(AUTHORIZATION, "Bearer " + login.getAccessToken());

        List<TempOrderDto> tempOrderDtoList = new ArrayList<>();
        TempOrderDto tempOrderDto1 = new TempOrderDto(PRODUCT1.getId(), 3);
        TempOrderDto tempOrderDto2 = new TempOrderDto(PRODUCT2.getId(), 3);
        tempOrderDtoList.add(tempOrderDto1);
        tempOrderDtoList.add(tempOrderDto2);

        TempOrderRequest tempOrderRequest = new TempOrderRequest(tempOrderDtoList);

        //when
        PostTempOrderResponse postTempOrderResponse = orderService.postTempOrder(tempOrderRequest, request);

        //then
        Long orderId = postTempOrderResponse.getOrderId();
        Order order = orderRepository.findById(orderId).get();
        List<OrderItem> orderItemList = order.getOrderItemList();
        Assertions.assertThat(orderItemList).isNotEmpty();
    }

    @Test
    @DisplayName("주문 불러오기 by seller")
    @Transactional
    void getOrderListBySeller() {
        //given
        Member member = memberRepository.findByEmail(MEMBER_EMAIL).get();
        Member sellerMember = memberRepository.findByEmail(SELLER_EMAIL).get();
        Category category = categoryRepository.findById(1L).get();

        ProductRequest searchDto1 = new ProductRequest(1L, "검색 제목", "메인 설명", "상품 메인 설명", "상품 서브 설명", 10000,
            8000, "보관 방법", "원산지", "생산자", "https://mainImage", "https://image1", "https://image2",
            "https://image3", "normal");
        ProductRequest searchDto2 = new ProductRequest(1L, "검색 제목2", "메인 설명", "상품 메인 설명", "상품 서브 설명", 15000,
            10000, "보관 방법", "원산지", "생산자", "https://mainImage", "https://image1", "https://image2",
            "https://image3", "normal");

        LoginRequest loginRequest = new LoginRequest(SELLER_EMAIL, "password");
        LoginResponse login = memberAuthService.login(loginRequest);

        Seller seller = sellerRepository.findByMember(sellerMember).get();
        Product saveProduct1 = productRepository.save(Product.of(seller, category, searchDto1));

        Member sellerMember2 = memberRepository.findByEmail(SELLER2_EMAIL).get();
        Seller seller2 = sellerRepository.findByMember(sellerMember2).get();
        Product saveProduct2 = productRepository.save(Product.of(seller2, category, searchDto2));

        Order order1 = orderRepository.save(Order.of(member, "테스트 상품"));
        orderItemRepository.save(OrderItem.of(order1, saveProduct1, 2));
        orderItemRepository.save(OrderItem.of(order1, saveProduct2, 5));
        order1.statusComplete();
        order1.orderNumber("ORDER-20001122-test");

        Order order2 = orderRepository.save(Order.of(member, "임시 상태 주문 상품"));

        Order order3 = orderRepository.save(Order.of(member, "테스트 상품2"));
        orderItemRepository.save(OrderItem.of(order3, saveProduct1, 10));
        orderItemRepository.save(OrderItem.of(order3, saveProduct2, 1));
        order3.statusComplete();
        order3.orderNumber("ORDER-20001122-test2");

        Order order4 = orderRepository.save(Order.of(member, "셀러1에게 표시되지 않을 상품"));
        orderItemRepository.save(OrderItem.of(order4, saveProduct2, 1));
        order4.statusComplete();
        order4.orderNumber("ORDER-20001122-test3");


        Pageable pageable = Pageable.ofSize(5);
        request.addHeader(AUTHORIZATION, "Bearer " + login.getAccessToken());

        //when
        OrderListResponse response = orderService.getOrderBySeller(pageable, "", null, request);

        //then
        Assertions.assertThat(response.getList().size()).isEqualTo(2);
    }
}