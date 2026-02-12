package com.xuan.it.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuan.it.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，自动提供基础的 CRUD 操作
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据用户ID查询订单列表
     * 注意：user_id 是分库字段，包含此条件可以避免全库扫描
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> selectByUserId(@Param("userId") Long userId);

    /**
     * 统计用户订单总金额
     *
     * @param userId 用户ID
     * @return 订单总金额
     */
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    /**
     * 根据订单状态查询订单列表
     * 注意：status 不是分片键，查询时会进行全库扫描，性能较差
     * 生产环境建议结合分片键查询，或使用 ES 等搜索引擎
     *
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> selectByStatus(@Param("status") String status);
}
