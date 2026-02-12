package com.xuan.it.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.it.entity.Order;
import com.xuan.it.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单服务类
 * 业务逻辑层完全不感知分库分表的存在
 */
@Service
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    /**
     * 创建订单
     * 与单表操作完全相同的代码，ShardingSphere 会自动路由到正确的库表
     *
     * @param userId 用户ID
     * @param amount 订单金额
     * @return 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long userId, BigDecimal amount) {
        Order order = new Order();
        order.setUserId(userId);
        order.setAmount(amount);
        order.setStatus("CREATED");

        // 保存订单，ShardingSphere 会：
        // 1. 使用雪花算法生成 order_id
        // 2. 根据 user_id % 2 决定存入 ds0 还是 ds1
        // 3. 根据 order_id % 2 决定存入 t_order_0 还是 t_order_1
        this.save(order);

        return order.getOrderId();
    }

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    /**
     * 统计用户订单总金额
     *
     * @param userId 用户ID
     * @return 订单总金额
     */
    public BigDecimal getTotalAmountByUserId(Long userId) {
        BigDecimal total = baseMapper.sumAmountByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * 根据订单ID查询订单详情
     * 注意：如果只有 order_id，ShardingSphere 需要查询所有表（2个库 * 2张表 = 4次查询）
     * 建议尽量同时提供 user_id 来优化性能
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    public Order getOrderById(Long orderId) {
        return this.getById(orderId);
    }

    /**
     * 更新订单状态
     *
     * @param orderId 订单ID
     * @param status  新状态
     * @return 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Long orderId, String status) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(status);
        return this.updateById(order);
    }

    /**
     * 批量插入测试数据
     * 用于验证分片规则是否正确
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertTestData() {
        System.out.println("========== 开始插入测试数据 ==========");

        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId((long) i);  // user_id 从 1 到 10
            order.setAmount(new BigDecimal(i * 100));
            order.setStatus("TEST");

            this.save(order);

            System.out.printf("插入订单: orderId=%d, userId=%d → 应该路由到: %s%n",
                    order.getOrderId(), order.getUserId(), calculateRouteInfo(order));
        }

        System.out.println("========== 插入完成 ==========");
    }

    /**
     * 根据分片规则计算路由信息（用于测试验证）
     *
     * @param order 订单对象
     * @return 路由信息
     */
    private String calculateRouteInfo(Order order) {
        // 分库计算：user_id % 2
        String database = (order.getUserId() % 2 == 0) ? "ds0" : "ds1";

        // 分表计算：order_id % 2
        String table = (order.getOrderId() % 2 == 0) ? "t_order_0" : "t_order_1";

        return String.format("库: %s, 表: %s", database, table);
    }
}
