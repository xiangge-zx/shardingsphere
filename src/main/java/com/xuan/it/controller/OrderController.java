package com.xuan.it.controller;

import com.xuan.it.dto.ApiResponse;
import com.xuan.it.dto.CreateOrderRequest;
import com.xuan.it.entity.Order;
import com.xuan.it.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单控制器
 * 提供订单相关的 REST API 接口
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单
     * POST /api/orders
     *
     * @param request 创建订单请求
     * @return 订单ID
     */
    @PostMapping
    public ApiResponse<Long> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Long orderId = orderService.createOrder(request.getUserId(), request.getAmount());
            return ApiResponse.success(orderId);
        } catch (Exception e) {
            return ApiResponse.error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 批量插入测试数据
     * POST /api/orders/test-batch
     *
     * @return 操作结果
     */
    @PostMapping("/test-batch")
    public ApiResponse<String> testBatchInsert() {
        try {
            orderService.batchInsertTestData();
            return ApiResponse.success("测试数据插入完成，请查看控制台输出和数据库验证");
        } catch (Exception e) {
            return ApiResponse.error("测试数据插入失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询订单列表
     * GET /api/orders/user/{userId}
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            return ApiResponse.success(orders);
        } catch (Exception e) {
            return ApiResponse.error("查询订单失败: " + e.getMessage());
        }
    }

    /**
     * 根据订单ID查询订单详情
     * GET /api/orders/{orderId}
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/{orderId}")
    public ApiResponse<Order> getOrderById(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return ApiResponse.error(404, "订单不存在");
            }
            return ApiResponse.success(order);
        } catch (Exception e) {
            return ApiResponse.error("查询订单失败: " + e.getMessage());
        }
    }

    /**
     * 统计用户订单总金额
     * GET /api/orders/user/{userId}/total
     *
     * @param userId 用户ID
     * @return 订单总金额
     */
    @GetMapping("/user/{userId}/total")
    public ApiResponse<BigDecimal> getTotalAmount(@PathVariable Long userId) {
        try {
            BigDecimal total = orderService.getTotalAmountByUserId(userId);
            return ApiResponse.success(total);
        } catch (Exception e) {
            return ApiResponse.error("统计失败: " + e.getMessage());
        }
    }

    /**
     * 更新订单状态
     * PUT /api/orders/{orderId}/status
     *
     * @param orderId 订单ID
     * @param status  新状态
     * @return 操作结果
     */
    @PutMapping("/{orderId}/status")
    public ApiResponse<String> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            boolean success = orderService.updateOrderStatus(orderId, status);
            if (success) {
                return ApiResponse.success("订单状态更新成功");
            } else {
                return ApiResponse.error("订单状态更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("订单状态更新失败: " + e.getMessage());
        }
    }
}
