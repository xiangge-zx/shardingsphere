package com.xuan.it;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ShardingSphere 分表示例项目启动类
 *
 * 功能说明：
 * 1. 演示如何使用 ShardingSphere-JDBC 实现分表
 * 2. 业务代码无感知：只操作逻辑表 t_order
 * 3. 主键使用雪花算法自动生成
 *
 * 分片规则：
 * - 分表字段：order_id（根据 order_id % 2 决定数据路由到 t_order_0 或 t_order_1）
 *
 * 启动前准备：
 * 1. 在 MySQL 中执行 init_database.sql 脚本，创建数据库和表
 * 2. 修改 application-sharding.yml 中内联的数据库连接信息（用户名、密码）
 * 3. 运行单元测试 ItApplicationTests 验证分表路由
 */
@SpringBootApplication
@MapperScan("com.xuan.it.mapper")
public class ItApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItApplication.class, args);
    }

}
