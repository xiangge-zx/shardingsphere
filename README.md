# ShardingSphere 分库分表示例项目

## 项目简介

本项目是一个基于 **Spring Boot 3.2.2** 和 **ShardingSphere-JDBC 5.5.0** 的分库分表示例项目，演示了如何使用 ShardingSphere 实现水平分库分表。

### 技术栈
- Spring Boot 3.2.2
- ShardingSphere-JDBC 5.5.0
- MyBatis-Plus 3.5.5
- MySQL 8.x
- Java 17 (LTS)
- Lombok

### 分片架构

```
逻辑表 t_order
    ├── ds0 (数据库0)
    │   ├── t_order_0 (表0)
    │   └── t_order_1 (表1)
    └── ds1 (数据库1)
        ├── t_order_0 (表0)
        └── t_order_1 (表1)
```

### 分片规则
- **分库字段**: `user_id`（根据 `user_id % 2` 决定数据存入 ds0 还是 ds1）
- **分表字段**: `order_id`（根据 `order_id % 2` 决定数据存入 t_order_0 还是 t_order_1）
- **主键生成**: 雪花算法（Snowflake），保证分布式环境下 ID 全局唯一

---

## 快速开始

### 1. 前置准备

确保你的环境中已安装：
- **JDK 17 或更高版本**（推荐 Java 17 LTS）
- Maven 3.6+
- MySQL 8.0+

> **注意**：如果你使用 Java 21，也可以正常运行，但建议使用 Java 17 以获得最佳兼容性。

### 2. 初始化数据库

在 MySQL 中执行项目根目录下的 `init_database.sql` 脚本：

```bash
mysql -u root -p < init_database.sql
```

该脚本会创建：
- 2 个数据库：`ds0`、`ds1`
- 每个库中 2 张表：`t_order_0`、`t_order_1`

### 3. 配置数据库连接

打开 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  shardingsphere:
    datasource:
      ds0:
        jdbc-url: jdbc:mysql://localhost:3306/ds0?...
        username: your_username_here  # 修改为你的 MySQL 用户名
        password: your_password_here  # 修改为你的 MySQL 密码
      ds1:
        jdbc-url: jdbc:mysql://localhost:3306/ds1?...
        username: your_username_here  # 修改为你的 MySQL 用户名
        password: your_password_here  # 修改为你的 MySQL 密码
```

### 4. 启动项目

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或者先编译再运行
mvn clean package
java -jar target/it-0.0.1-SNAPSHOT.jar
```

启动成功后，控制台会显示：

```
========================================
ShardingSphere 示例项目启动成功！
========================================
测试接口：
  - POST   http://localhost:8080/api/orders/test-batch
  - POST   http://localhost:8080/api/orders
  - GET    http://localhost:8080/api/orders/user/{userId}
  - GET    http://localhost:8080/api/orders/{orderId}
========================================
```

---

## API 接口说明

### 1. 批量插入测试数据

**请求示例**：
```bash
curl -X POST http://localhost:8080/api/orders/test-batch
```

**功能**：插入 10 条测试订单，user_id 从 1 到 10，控制台会输出每条数据的路由信息。

**控制台输出示例**：
```
========== 开始插入测试数据 ==========
插入订单: orderId=123456789, userId=1 → 应该路由到: 库: ds1, 表: t_order_1
插入订单: orderId=123456790, userId=2 → 应该路由到: 库: ds0, 表: t_order_0
...
========== 插入完成 ==========
```

### 2. 创建订单

**请求示例**：
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 100,
    "amount": 999.99
  }'
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1755892345678901234
}
```

### 3. 根据用户ID查询订单

**请求示例**：
```bash
curl -X GET http://localhost:8080/api/orders/user/1
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "orderId": 1755892345678901234,
      "userId": 1,
      "amount": 100.00,
      "status": "TEST",
      "createTime": "2026-02-11T10:30:00"
    }
  ]
}
```

### 4. 根据订单ID查询订单详情

**请求示例**：
```bash
curl -X GET http://localhost:8080/api/orders/1755892345678901234
```

### 5. 统计用户订单总金额

**请求示例**：
```bash
curl -X GET http://localhost:8080/api/orders/user/1/total
```

### 6. 更新订单状态

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/api/orders/1755892345678901234/status?status=PAID"
```

---

## 验证分片效果

### 方法1：查看控制台 SQL 日志

由于配置了 `sql-show: true`，控制台会显示实际执行的 SQL：

```
Logic SQL: INSERT INTO t_order (user_id, amount, status, create_time) VALUES (?, ?, ?, ?)
Actual SQL: ds1 ::: INSERT INTO t_order_1 (order_id, user_id, amount, status, create_time) VALUES (?, ?, ?, ?, ?)
```

### 方法2：查询数据库验证

```sql
-- 查看 ds0 数据库的表数据
USE ds0;
SELECT COUNT(*) FROM t_order_0;
SELECT COUNT(*) FROM t_order_1;

-- 查看 ds1 数据库的表数据
USE ds1;
SELECT COUNT(*) FROM t_order_0;
SELECT COUNT(*) FROM t_order_1;
```

理论上，数据应该均匀分布在 4 张表中。

---

## 核心配置说明

### 1. 分片规则（application.yml）

```yaml
tables:
  t_order:
    # 实际数据节点：Groovy 表达式，表示 ds0.t_order_0, ds0.t_order_1, ds1.t_order_0, ds1.t_order_1
    actual-data-nodes: ds$->{0..1}.t_order_$->{0..1}

    # 分库策略：根据 user_id 取模
    database-strategy:
      standard:
        sharding-column: user_id
        sharding-algorithm-name: database-inline

    # 分表策略：根据 order_id 取模
    table-strategy:
      standard:
        sharding-column: order_id
        sharding-algorithm-name: table-inline
```

### 2. 分片算法

```yaml
sharding-algorithms:
  database-inline:
    type: MOD
    props:
      sharding-count: 2  # 2个库
  table-inline:
    type: MOD
    props:
      sharding-count: 2  # 2张表
```

### 3. 雪花算法配置

```yaml
key-generators:
  snowflake_gen:
    type: SNOWFLAKE
    props:
      worker-id: 1  # 工作机器 ID，生产环境需确保唯一
```

---

## 常见问题

### Q0: 启动报错 "Invalid bean definition with name 'orderMapper'"

**原因**: Spring Boot 版本与 MyBatis-Plus/ShardingSphere 不兼容。

**解决方案**:
- 确保使用 Spring Boot 3.2.2（不要使用 4.x 版本）
- 确保 Java 版本为 17 或更高
- 清理并重新下载依赖：`mvn clean install -U`
- 详细说明请查看 `FIX_GUIDE.md` 文件

### Q1: 启动报错 "No qualifying bean of type 'javax.sql.DataSource'"

**原因**: ShardingSphere 依赖未正确加载。

**解决方案**: 确保 `pom.xml` 中已添加 `shardingsphere-jdbc-core-spring-boot-starter` 依赖。

### Q2: 插入数据时报错 "Cannot find actual data source node"

**原因**: 分片规则配置错误，或数据库连接失败。

**解决方案**:
1. 检查 `application.yml` 中的数据库连接信息是否正确
2. 确保数据库和表已正确创建
3. 检查 `actual-data-nodes` 配置是否与实际表名匹配

### Q3: 只有 order_id 查询时性能很慢

**原因**: 缺少分片键 `user_id`，导致 ShardingSphere 需要查询所有分片（全库扫描）。

**解决方案**:
1. 尽量在查询条件中包含分片键 `user_id`
2. 对于非分片键查询，可考虑建立索引表或使用 Elasticsearch

### Q4: 生产环境如何保证 worker-id 唯一？

**解决方案**:
1. 通过配置中心（Nacos、Apollo）为每个实例分配唯一的 worker-id
2. 通过数据库表记录已分配的 worker-id
3. 使用 IP 地址或机器名生成唯一标识

---

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/xuan/it/
│   │       ├── ItApplication.java        # 启动类
│   │       ├── entity/
│   │       │   └── Order.java            # 订单实体类
│   │       ├── mapper/
│   │       │   └── OrderMapper.java      # 订单 Mapper 接口
│   │       ├── service/
│   │       │   └── OrderService.java     # 订单服务类
│   │       ├── controller/
│   │       │   └── OrderController.java  # 订单控制器
│   │       └── dto/
│   │           ├── CreateOrderRequest.java  # 创建订单请求 DTO
│   │           └── ApiResponse.java         # 统一响应 DTO
│   └── resources/
│       └── application.yml               # 配置文件
└── test/
    └── java/
        └── com/xuan/it/
            └── ItApplicationTests.java   # 测试类
```

---

## 参考资料

- [ShardingSphere 官方文档](https://shardingsphere.apache.org/document/current/cn/overview/)
- [ShardingSphere-JDBC 配置手册](https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/)
- [MyBatis-Plus 官方文档](https://baomidou.com/)

---

## License

MIT License
