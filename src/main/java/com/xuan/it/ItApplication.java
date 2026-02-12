package com.xuan.it;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ShardingSphere 分库分表示例项目启动类
 *
 * 功能说明：
 * 1. 演示如何使用 ShardingSphere-JDBC 实现分库分表
 * 2. 配置了 2 个数据库（ds0, ds1）和 4 张物理表（每个库 2 张表）
 * 3. 业务代码完全无感知，只操作逻辑表 t_order
 * 4. 自动使用雪花算法生成分布式 ID
 *
 * 分片规则：
 * - 分库字段：user_id（根据 user_id % 2 决定数据存入 ds0 还是 ds1）
 * - 分表字段：order_id（根据 order_id % 2 决定数据存入 t_order_0 还是 t_order_1）
 *
 * 启动前准备：
 * 1. 在 MySQL 中执行 init_database.sql 脚本，创建数据库和表
 * 2. 修改 application.yml 中的数据库连接信息（用户名、密码）
 * 3. 确保 MySQL 服务已启动
 *
 * 测试方式：
 * 1. 启动项目后，访问 http://localhost:8080/api/orders/test-batch 插入测试数据
 * 2. 观察控制台输出的 SQL 日志，查看数据路由情况
 * 3. 登录 MySQL 查看 ds0 和 ds1 中各表的数据分布
 */
@SpringBootApplication
@MapperScan("com.xuan.it.mapper")
public class ItApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("ShardingSphere 示例项目启动成功！");
        System.out.println("========================================");
        System.out.println("测试接口：");
        System.out.println("  - POST   http://localhost:8080/api/orders/test-batch");
        System.out.println("  - POST   http://localhost:8080/api/orders");
        System.out.println("  - GET    http://localhost:8080/api/orders/user/{userId}");
        System.out.println("  - GET    http://localhost:8080/api/orders/{orderId}");
        System.out.println("========================================\n");
    }

}
