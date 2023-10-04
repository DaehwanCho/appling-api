package com.juno.appling.order.presentation;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders;
import com.juno.appling.ControllerBaseTest;
import com.juno.appling.member.application.MemberAuthService;
import com.juno.appling.member.domain.Member;
import com.juno.appling.member.domain.MemberRepository;
import com.juno.appling.member.domain.Seller;
import com.juno.appling.member.domain.SellerRepository;
import com.juno.appling.member.dto.request.LoginRequest;
import com.juno.appling.member.dto.response.LoginResponse;
import com.juno.appling.order.domain.Order;
import com.juno.appling.order.domain.OrderItem;
import com.juno.appling.order.domain.OrderItemRepository;
import com.juno.appling.order.domain.OrderRepository;
import com.juno.appling.order.dto.request.CompleteOrderRequest;
import com.juno.appling.order.dto.request.TempOrderDto;
import com.juno.appling.order.dto.request.TempOrderRequest;
import com.juno.appling.product.domain.Category;
import com.juno.appling.product.domain.CategoryRepository;
import com.juno.appling.product.domain.Product;
import com.juno.appling.product.domain.ProductRepository;
import com.juno.appling.product.dto.request.ProductRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
class OrderControllerDocs extends ControllerBaseTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberAuthService memberAuthService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private ObjectMapper objectMapper = new ObjectMapper();


    private final static String PREFIX = "/api/order";

    @Test
    @DisplayName(PREFIX + " (POST)")
    void postTempOrder() throws Exception {
        //given
        Member member = memberRepository.findByEmail(SELLER_EMAIL).get();
        Category category = categoryRepository.findById(1L).get();

        ProductRequest searchDto1 = new ProductRequest(1L, "검색 제목", "메인 설명", "상품 메인 설명", "상품 서브 설명", 10000,
                8000, "보관 방법", "원산지", "생산자", "https://mainImage", "https://image1", "https://image2",
                "https://image3", "normal");
        ProductRequest searchDto2 = new ProductRequest(1L, "검색 제목2", "메인 설명", "상품 메인 설명", "상품 서브 설명", 15000,
                10000, "보관 방법", "원산지", "생산자", "https://mainImage", "https://image1", "https://image2",
                "https://image3", "normal");

        LoginRequest loginRequest = new LoginRequest(MEMBER_EMAIL, "password");
        LoginResponse login = memberAuthService.login(loginRequest);

        Seller seller = sellerRepository.findByMember(member).get();
        Product saveProduct1 = productRepository.save(Product.of(seller, category, searchDto1));
        Product saveProduct2 = productRepository.save(Product.of(seller, category, searchDto2));

        List<TempOrderDto> orderList = new ArrayList<>();
        int orderEa1 = 3;
        int orderEa2 = 2;
        TempOrderDto order1 = new TempOrderDto(saveProduct1.getId(), orderEa1);
        TempOrderDto order2 = new TempOrderDto(saveProduct2.getId(), orderEa2);
        orderList.add(order1);
        orderList.add(order2);
        TempOrderRequest tempOrderRequest = new TempOrderRequest(orderList);
        //when
        ResultActions perform = mock.perform(
                post(PREFIX).contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION, "Bearer " + login.getAccessToken())
                        .content(objectMapper.writeValueAsString(tempOrderRequest))
        );
        //then
        perform.andExpect(status().is2xxSuccessful());
        perform.andDo(docs.document(
                requestHeaders(
                        headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
                ),
                requestFields(
                        fieldWithPath("order_list").type(JsonFieldType.ARRAY).description("주문 리스트"),
                        fieldWithPath("order_list[].product_id").type(JsonFieldType.NUMBER).description("상품 id"),
                        fieldWithPath("order_list[].ea").type(JsonFieldType.NUMBER).description("주문 개수")
                ),
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                        fieldWithPath("data.order_id").type(JsonFieldType.NUMBER).description("주문 id")
                )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/temp/{order_id} (GET)")
    void getTempOrder() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest(MEMBER_EMAIL, "password");
        LoginResponse login = memberAuthService.login(loginRequest);
        Member member = memberRepository.findByEmail(MEMBER_EMAIL).get();

        Order order = orderRepository.save(Order.of(member, "테스트 상품"));

        Member sellerMember = memberRepository.findByEmail(SELLER_EMAIL).get();
        Category category = categoryRepository.findById(1L).get();

        ProductRequest searchDto1 = new ProductRequest(1L, "검색 제목", "메인 설명", "상품 메인 설명", "상품 서브 설명", 10000,
                8000, "보관 방법", "원산지", "생산자", "https://mainImage", "https://image1", "https://image2",
                "https://image3", "normal");
        ProductRequest searchDto2 = new ProductRequest(1L, "검색 제목2", "메인 설명", "상품 메인 설명", "상품 서브 설명", 15000,
                10000, "보관 방법", "원산지", "생산자", "https://mainImage", "https://image1", "https://image2",
                "https://image3", "normal");
        Seller seller = sellerRepository.findByMember(sellerMember).get();
        Product saveProduct1 = productRepository.save(Product.of(seller, category, searchDto1));
        Product saveProduct2 = productRepository.save(Product.of(seller, category, searchDto2));

        orderItemRepository.save(OrderItem.of(order, saveProduct1, 3));
        orderItemRepository.save(OrderItem.of(order, saveProduct2, 5));

        //when
        ResultActions perform = mock.perform(
                get(PREFIX + "/temp/{order_id}", order.getId())
                        .header(AUTHORIZATION, "Bearer " + login.getAccessToken())
        );
        //then
        perform.andExpect(status().is2xxSuccessful());
        perform.andDo(docs.document(
                requestHeaders(
                        headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
                ),
                pathParameters(
                        parameterWithName("order_id").description("주문 id")
                ),
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                        fieldWithPath("data.order_id").type(JsonFieldType.NUMBER).description("주문 id"),
                        fieldWithPath("data.order_item_list").type(JsonFieldType.ARRAY).description("주문 상품 리스트"),
                        fieldWithPath("data.order_item_list[].product_id").type(JsonFieldType.NUMBER).description("상품 id"),
                        fieldWithPath("data.order_item_list[].ea").type(JsonFieldType.NUMBER).description("상품 주문 개수"),
                        fieldWithPath("data.order_item_list[].main_title").type(JsonFieldType.STRING).description("주문 상품명"),
                        fieldWithPath("data.order_item_list[].main_explanation").type(JsonFieldType.STRING).description("주문 상품 메인 설명"),
                        fieldWithPath("data.order_item_list[].product_main_explanation").type(JsonFieldType.STRING).description("주문 상품 상세 설명"),
                        fieldWithPath("data.order_item_list[].product_sub_explanation").type(JsonFieldType.STRING).description("주문 상품 상세 서브 설명"),
                        fieldWithPath("data.order_item_list[].origin_price").type(JsonFieldType.NUMBER).description("주문 상품 원가"),
                        fieldWithPath("data.order_item_list[].price").type(JsonFieldType.NUMBER).description("주문 상품 판매가"),
                        fieldWithPath("data.order_item_list[].purchase_inquiry").type(JsonFieldType.STRING).description("주문 상품 취급 주의 사항"),
                        fieldWithPath("data.order_item_list[].origin").type(JsonFieldType.STRING).description("원산지"),
                        fieldWithPath("data.order_item_list[].producer").type(JsonFieldType.STRING).description("공급자"),
                        fieldWithPath("data.order_item_list[].main_image").type(JsonFieldType.STRING).description("메인 이미지"),
                        fieldWithPath("data.order_item_list[].image1").type(JsonFieldType.STRING).description("이미지1"),
                        fieldWithPath("data.order_item_list[].image2").type(JsonFieldType.STRING).description("이미지2"),
                        fieldWithPath("data.order_item_list[].image3").type(JsonFieldType.STRING).description("이미지3"),
                        fieldWithPath("data.order_item_list[].view_cnt").type(JsonFieldType.NUMBER).description("조회수"),
                        fieldWithPath("data.order_item_list[].status").type(JsonFieldType.STRING).description("상품 상태"),
                        fieldWithPath("data.order_item_list[].created_at").type(JsonFieldType.STRING).description("상품 생성일"),
                        fieldWithPath("data.order_item_list[].modified_at").type(JsonFieldType.STRING).description("상품 수정일"),
                        fieldWithPath("data.order_item_list[].seller").type(JsonFieldType.OBJECT).description("상품 판매자"),
                        fieldWithPath("data.order_item_list[].seller.seller_id").type(JsonFieldType.NUMBER).description("상품 판매자 id"),
                        fieldWithPath("data.order_item_list[].seller.email").type(JsonFieldType.STRING).description("상품 판매자 email"),
                        fieldWithPath("data.order_item_list[].seller.company").type(JsonFieldType.STRING).description("상품 판매자 회사명"),
                        fieldWithPath("data.order_item_list[].seller.zonecode").type(JsonFieldType.STRING).description("상품 판매자 우편 주소"),
                        fieldWithPath("data.order_item_list[].seller.address").type(JsonFieldType.STRING).description("상품 판매자 주소"),
                        fieldWithPath("data.order_item_list[].seller.address_detail").type(JsonFieldType.STRING).description("상품 판매자 상세 주소"),
                        fieldWithPath("data.order_item_list[].seller.tel").type(JsonFieldType.STRING).description("상품 판매자 연락처"),
                        fieldWithPath("data.order_item_list[].category").type(JsonFieldType.OBJECT).description("상품 카테고리"),
                        fieldWithPath("data.order_item_list[].category.category_id").type(JsonFieldType.NUMBER).description("상품 카테고리 id"),
                        fieldWithPath("data.order_item_list[].category.name").type(JsonFieldType.STRING).description("상품 카테고리명"),
                        fieldWithPath("data.order_item_list[].category.created_at").type(JsonFieldType.STRING).description("상품 카테고리 생성일"),
                        fieldWithPath("data.order_item_list[].category.modified_at").type(JsonFieldType.STRING).description("상품 카테고리 수정일")
                )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/complete (PATCH)")
    void complete() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest(MEMBER_EMAIL, "password");
        LoginResponse login = memberAuthService.login(loginRequest);
        Member member = memberRepository.findByEmail(MEMBER_EMAIL).get();

        Order order = orderRepository.save(Order.of(member, "테스트 상품"));
        Long orderId = order.getId();

        Member sellerMember = memberRepository.findByEmail(SELLER_EMAIL).get();
        Category category = categoryRepository.findById(1L).get();

        ProductRequest searchDto1 = new ProductRequest(1L, "검색 제목", "메인 설명", "상품 메인 설명", "상품 서브 설명", 10000,
                8000, "보관 방법", "원산지", "생산자", "https://mainImage", "https://image1", "https://image2",
                "https://image3", "normal");
        ProductRequest searchDto2 = new ProductRequest(1L, "검색 제목2", "메인 설명", "상품 메인 설명", "상품 서브 설명", 15000,
                10000, "보관 방법", "원산지", "생산자", "https://mainImage", "https://image1", "https://image2",
                "https://image3", "normal");
        Seller seller = sellerRepository.findByMember(sellerMember).get();
        Product saveProduct1 = productRepository.save(Product.of(seller, category, searchDto1));
        Product saveProduct2 = productRepository.save(Product.of(seller, category, searchDto2));

        orderItemRepository.save(OrderItem.of(order, saveProduct1, 3));
        orderItemRepository.save(OrderItem.of(order, saveProduct2, 5));

        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest(orderId, "주문자", "주문자 우편번호", "주문자 주소", "주문자 상세 주소", "주문자 연락처", "수령인", "수령인 우편번호", "수령인 주소", "수령인 상세 주소", "수령인 연락처");
        //when
        ResultActions perform = mock.perform(
                patch(PREFIX + "/complete")
                        .header(AUTHORIZATION, "Bearer " + login.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeOrderRequest))
        );
        //then
        perform.andDo(docs.document(
                requestHeaders(
                        headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
                ),
                requestFields(
                        fieldWithPath("order_id").description("주문 id"),
                        fieldWithPath("owner_name").description("주문자"),
                        fieldWithPath("owner_zonecode").description("주문자 우편번호"),
                        fieldWithPath("owner_address").description("주문자 주소"),
                        fieldWithPath("owner_address_detail").description("주문자 상세 주소"),
                        fieldWithPath("owner_tel").description("주문자 연락처"),
                        fieldWithPath("recipient_name").description("수령인"),
                        fieldWithPath("recipient_zonecode").description("수령인 우편번호"),
                        fieldWithPath("recipient_address").description("수령인 주소"),
                        fieldWithPath("recipient_address_detail").description("수령인 상세 주소"),
                        fieldWithPath("recipient_tel").description("수령인 연락처")
                ),
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                        fieldWithPath("data.order_id").type(JsonFieldType.NUMBER).description("주문 id"),
                        fieldWithPath("data.order_number").type(JsonFieldType.STRING).description("주문 번호")
                )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/seller/list (GET)")
    @Transactional
    void getOrderListBySeller() throws Exception {
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

        //when
        ResultActions perform = mock.perform(
            get(PREFIX + "/seller/list")
                .header(AUTHORIZATION, "Bearer " + login.getAccessToken())
                .queryParam("search", "")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("status", "")
        );


        //then
        perform.andExpect(status().is2xxSuccessful());
        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token (SELLER 권한 이상)")
            ),
            queryParameters(
                parameterWithName("search").description("검색어"),
                parameterWithName("page").description("page 번호"),
                parameterWithName("size").description("요청당 반환 받을 리스트 크기"),
                parameterWithName("status").description("주문 상태 / order:주문중, complete:주문완료, cancel:취소")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.total_page").type(JsonFieldType.NUMBER).description("페이지 총 크기"),
                fieldWithPath("data.total_elements").type(JsonFieldType.NUMBER).description("반환 데이터 총 크기"),
                fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("페이지 마지막 유무"),
                fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("페이지 빈값 유무"),
                fieldWithPath("data.list").type(JsonFieldType.ARRAY).description("주문 리스트"),
            )
        ));
    }
}