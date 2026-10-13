package com.acme.orders;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrdersController {
    @GetMapping("/api/orders")
    public String getOrders() {
        return "orders-service";
    }
}
