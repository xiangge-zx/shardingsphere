# ShardingSphere 分表示例项目（最小化配置）

## 项目简介

本项目用于用**最少的配置**演示 ShardingSphere-JDBC 的**分表**：应用只操作逻辑表 `t_order`，实际根据 `order_id % 2` 路由到物理表 `t_order_0` / `t_order_1`。

- Spring Boot 3.2.2
- ShardingSphere-JDBC 5.5.0
- MyBatis-Plus 3.x
- Java 17
- MySQL 8.x

## 分表规则

- **逻辑表**：`t_order`
- **物理表**：`t_order_0`、`t_order_1`
- **分片键**：`order_id`
- **分片算法**：`t_order_${order_id % 2}`
- **主键生成**：Snowflake（雪花算法）

## 快速开始

### 1. 初始化数据库

在 MySQL 执行根目录下的 `init_database.sql`：

```bash
mysql -u root -p < init_database.sql
```

脚本会创建数据库 `ds01`，并创建如下表：

- `t_order_0`
- `t_order_1`
- （可选对照）`t_order`

### 2. 配置数据库连接

本项目把 ShardingSphere 的 YAML 规则**内联**在 `src/main/resources/application-sharding.yml` 里（不再使用 `jdbc:shardingsphere:classpath:...` 这种跳转写法）。

你只需要修改 `src/main/resources/application-sharding.yml` 里的 `app.shardingsphere.yaml` 段落（示例）：

```yaml
dataSources:
  ds0:
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://127.0.0.1:3306/ds01?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: your_password
```

### 3. 运行单元测试验证分表

推荐直接跑测试类：`src/test/java/com/xuan/it/ItApplicationTests.java`

```bash
mvn test
```

> 如果你执行 `mvn -v` 看到的是 `Java version: 1.8.x`，请先把 `JAVA_HOME` 指向 JDK 17+（并确保 `%JAVA_HOME%/bin` 在 PATH 前面），否则会出现 `Invalid flag: --release`。

测试点（示例）：

- `testCreateOrder()`：创建订单 → 按 `orderId` 回查
- `testShardingRouting()`：批量插入 → 按 `userId` 查询（验证业务读写正常）

### 4. （可选）手动查库验证

```sql
USE ds01;
SELECT COUNT(*) FROM t_order_0;
SELECT COUNT(*) FROM t_order_1;
```

## 核心配置文件

- `src/main/resources/application.yml`：只负责选择 profile（默认 `sharding`）
- `src/main/resources/application-sharding.yml`

## 项目结构（已移除 Web/Controller）

```
src/
├── main/
│   ├── java/
│   │   └── com/xuan/it/
│   │       ├── ItApplication.java
│   │       ├── entity/
│   │       ├── mapper/
│   │       └── service/
│   └── resources/
│       ├── application.yml
│       ├── application-sharding.yml
│       └── (无额外 sharding/*.yaml 文件)
└── test/
    └── java/
        └── com/xuan/it/
            └── ItApplicationTests.java
```

