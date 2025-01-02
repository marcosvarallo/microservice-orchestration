package com.microservices.orchestration.core.controller;

import com.microservices.orchestration.core.document.Order;
import com.microservices.orchestration.core.dto.OrderRequest;
import com.microservices.orchestration.core.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Order createOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }
}
