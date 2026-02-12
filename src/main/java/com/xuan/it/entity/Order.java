package com.xuan.it.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 对应逻辑表 t_order，实际会路由到 ds0/ds1 的 t_order_0/t_order_1
 */
@Data
@TableName("t_order")
public class Order {

    /**
     * 订单ID - 主键
     * 使用雪花算法自动生成，全局唯一
     * 同时也是分表字段：根据 order_id % 2 决定存入 t_order_0 还是 t_order_1
     */
    @TableId
    private Long orderId;

    /**
     * 用户ID
     * 分库字段：根据 user_id % 2 决定存入 ds0 还是 ds1
     */
    private Long userId;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单状态
     * 如：CREATED(已创建)、PAID(已支付)、SHIPPED(已发货)、COMPLETED(已完成)
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 构造函数：自动设置创建时间
     */
    public Order() {
        this.createTime = LocalDateTime.now();
    }
}
