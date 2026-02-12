package com.xuan.it;

import com.xuan.it.entity.Order;
import com.xuan.it.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ShardingSphere 分库分表测试类
 */
@SpringBootTest
class ItApplicationTests {

    @Autowired
    private OrderService orderService;

    /**
     * 测试上下文加载
     */
    @Test
    void contextLoads() {
        assertNotNull(orderService);
    }

    /**
     * 测试创建订单
     */
    @Test
    void testCreateOrder() {
        // 创建订单
        Long orderId = orderService.createOrder(100L, new BigDecimal("999.99"));

        // 验证订单ID不为空
        assertNotNull(orderId);
        System.out.println("创建订单成功，订单ID: " + orderId);

        // 查询订单
        Order order = orderService.getOrderById(orderId);
        assertNotNull(order);
        assertEquals(100L, order.getUserId());
        assertEquals(new BigDecimal("999.99"), order.getAmount());
    }

    /**
     * 测试批量插入和分片路由
     */
    @Test
    void testShardingRouting() {
        System.out.println("\n========== 测试分片路由 ==========");

        // 插入测试数据
        orderService.batchInsertTestData();

        // 验证数据是否插入成功
        List<Order> userOrders = orderService.getOrdersByUserId(1L);
        assertNotNull(userOrders);
        assertFalse(userOrders.isEmpty());

        System.out.println("用户 1 的订单数量: " + userOrders.size());
    }

    /**
     * 测试根据用户ID查询订单
     */
    @Test
    void testGetOrdersByUserId() {
        // 先插入几条测试数据
        orderService.createOrder(200L, new BigDecimal("100.00"));
        orderService.createOrder(200L, new BigDecimal("200.00"));
        orderService.createOrder(200L, new BigDecimal("300.00"));

        // 查询用户订单
        List<Order> orders = orderService.getOrdersByUserId(200L);

        // 验证
        assertNotNull(orders);
        assertTrue(orders.size() >= 3);
        orders.forEach(order -> assertEquals(200L, order.getUserId()));

        System.out.println("用户 200 的订单数量: " + orders.size());
    }

    /**
     * 测试统计用户订单总金额
     */
    @Test
    void testGetTotalAmountByUserId() {
        // 插入测试数据
        orderService.createOrder(300L, new BigDecimal("100.00"));
        orderService.createOrder(300L, new BigDecimal("200.00"));
        orderService.createOrder(300L, new BigDecimal("300.00"));

        // 统计总金额
        BigDecimal total = orderService.getTotalAmountByUserId(300L);

        // 验证
        assertNotNull(total);
        assertTrue(total.compareTo(new BigDecimal("600.00")) >= 0);

        System.out.println("用户 300 的订单总金额: " + total);
    }

    /**
     * 测试更新订单状态
     */
    @Test
    void testUpdateOrderStatus() {
        // 先创建订单
        Long orderId = orderService.createOrder(400L, new BigDecimal("500.00"));

        // 更新订单状态
        boolean success = orderService.updateOrderStatus(orderId, "PAID");
        assertTrue(success);

        // 验证状态已更新
        Order order = orderService.getOrderById(orderId);
        assertNotNull(order);
        assertEquals("PAID", order.getStatus());

        System.out.println("订单状态更新成功: " + orderId + " -> PAID");
    }

}
