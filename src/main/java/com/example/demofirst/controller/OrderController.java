package com.example.demofirst.controller;


import com.example.demofirst.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 创建订单（高并发测试接口）
     */
    @PostMapping("/create")
    public String createOrder(
            @RequestParam String userId,
            @RequestParam String goodsId) {
        return orderService.createOrder(userId, goodsId);
    }
}
