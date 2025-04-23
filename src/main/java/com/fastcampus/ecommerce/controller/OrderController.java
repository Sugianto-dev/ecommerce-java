package com.fastcampus.ecommerce.controller;

import com.fastcampus.ecommerce.common.PageUtil;
import com.fastcampus.ecommerce.common.errors.BadRequestException;
import com.fastcampus.ecommerce.entity.Order;
import com.fastcampus.ecommerce.model.*;
import com.fastcampus.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("orders")
@SecurityRequirement(name = "Bearer")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest checkoutRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        checkoutRequest.setUserId(userInfo.getUser().getUserId());
        OrderResponse orderResponse = orderService.checkout(checkoutRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findOrderById(@PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        return orderService.findOrderById(orderId)
                .map(order -> {
                    if (!order.getUserId().equals(userInfo.getUser().getUserId())) {
                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(OrderResponse.builder().build());
                    }
                    OrderResponse response = OrderResponse.fromOrder(order);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("")
    public ResponseEntity<PaginatedOrderResponse> findOrdersByUserId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "order_id,desc") String[] sort
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        List<Sort.Order> sortOrder = PageUtil.parseSortOrderRequest(sort);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder));

        Page<OrderResponse> userOrders = orderService.findOrdersByUserIdAndPageable(userInfo.getUser().getUserId(), pageable);
        PaginatedOrderResponse paginatedOrderResponse = orderService.convertOrderPage(userOrders);

        return ResponseEntity.ok(paginatedOrderResponse);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItemResponse>> findOrderItems(@PathVariable Long orderId) {
        List<OrderItemResponse> orderItemResponses = orderService.findOrderItemsByOrderId(orderId);
        return ResponseEntity.ok(orderItemResponses);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long orderId, @RequestParam String newStatus) {
        OrderStatus status = null;
        try {
            status = OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unrecognize status :" + status);
        }
        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/total")
    public ResponseEntity<Double> calculateOrderTotal(@PathVariable Long orderId) {
        Double total = orderService.calculateOrderTotal(orderId);
        return ResponseEntity.ok(total);
    }
}
