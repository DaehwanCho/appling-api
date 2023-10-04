package com.juno.appling.order.presentation;

import com.juno.appling.global.base.Api;
import com.juno.appling.global.base.ResultCode;
import com.juno.appling.order.application.OrderService;
import com.juno.appling.order.dto.request.CompleteOrderRequest;
import com.juno.appling.order.dto.request.TempOrderRequest;
import com.juno.appling.order.dto.response.CompleteOrderResponse;
import com.juno.appling.order.dto.response.OrderListResponse;
import com.juno.appling.order.dto.response.TempOrderResponse;
import com.juno.appling.order.dto.response.PostTempOrderResponse;
import com.juno.appling.order.enums.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api-prefix}/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Api<PostTempOrderResponse>> postOrder(@RequestBody @Validated TempOrderRequest tempOrderRequest, HttpServletRequest request, BindingResult bindingResult) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Api.<PostTempOrderResponse>builder()
                                .code(ResultCode.POST.code)
                                .message(ResultCode.POST.message)
                                .data(orderService.postTempOrder(tempOrderRequest, request))
                                .build()
                );
    }

    @GetMapping("/temp/{order_id}")
    public ResponseEntity<Api<TempOrderResponse>> getTempOrder(@PathVariable(name = "order_id") Long orderId, HttpServletRequest request) {
        return ResponseEntity.ok(
                Api.<TempOrderResponse>builder()
                        .code(ResultCode.SUCCESS.code)
                        .message(ResultCode.SUCCESS.message)
                        .data(orderService.getTempOrder(orderId, request))
                        .build()
        );
    }

    @PatchMapping("/complete")
    public ResponseEntity<Api<CompleteOrderResponse>> completeOrder(@RequestBody @Validated CompleteOrderRequest completeOrderRequest, HttpServletRequest request, BindingResult bindingResult) {
        return ResponseEntity.ok(Api.<CompleteOrderResponse>builder()
                .code(ResultCode.SUCCESS.code)
                .message(ResultCode.SUCCESS.message)
                .data(orderService.completeOrder(completeOrderRequest, request))
                .build());
    }

    @GetMapping("/seller/list")
    public ResponseEntity<Api<OrderListResponse>> getOrderListBySeller( @PageableDefault(size = 10, page = 0) Pageable pageable,
        @RequestParam(required = false, name = "search") String search,
        @RequestParam(required = false, name = "status", defaultValue = "") String status,
        HttpServletRequest request) {
        return ResponseEntity.ok(Api.<OrderListResponse>builder()
            .code(ResultCode.SUCCESS.code)
            .message(ResultCode.SUCCESS.message)
            .data(orderService.getOrderBySeller(pageable, search, status, request))
            .build());
    }
}
