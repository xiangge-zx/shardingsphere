package com.xuan.it.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单请求 DTO
 */
@Data
public class CreateOrderRequest {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单金额
     */
    private BigDecimal amount;
}
