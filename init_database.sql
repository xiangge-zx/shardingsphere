-- ============================================================
-- ShardingSphere 分表（可禁用）数据库初始化脚本
-- ============================================================
-- 说明：
-- 1. 本脚本创建 1 个数据库（ds01）
-- 2. 同时创建：
--    - 单表模式物理表：t_order
--    - 分表模式物理表：t_order_0、t_order_1
-- 3. 应用程序只操作逻辑表 t_order：
--    - 开启分表 profile 时路由到 t_order_0/1
--    - 禁用分表 profile 时直接落到 t_order
-- 4. 分表规则：根据 order_id % 2 决定存入 t_order_0 还是 t_order_1
-- ============================================================

-- ============================================================
-- 创建数据库
-- ============================================================

-- 删除旧数据库（慎用，会丢失所有数据）
-- DROP DATABASE IF EXISTS ds01;

-- 创建数据库 ds01
CREATE DATABASE IF NOT EXISTS ds01
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ds01;

-- ============================================================
-- 单表模式：t_order
-- ============================================================
CREATE TABLE IF NOT EXISTS t_order (
    order_id BIGINT NOT NULL COMMENT '订单ID（主键，雪花算法生成）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED' COMMENT '订单状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (order_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='订单表（ds01库，单表模式）';

-- ============================================================
-- 分表模式：t_order_0 / t_order_1
-- ============================================================
CREATE TABLE IF NOT EXISTS t_order_0 (
    order_id BIGINT NOT NULL COMMENT '订单ID（主键，雪花算法生成）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED' COMMENT '订单状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (order_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='订单表0（ds01库，分表模式）';

CREATE TABLE IF NOT EXISTS t_order_1 (
    order_id BIGINT NOT NULL COMMENT '订单ID（主键，雪花算法生成）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED' COMMENT '订单状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (order_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='订单表1（ds01库，分表模式）';

-- ============================================================
-- 验证表结构
-- ============================================================
SHOW TABLES;

-- ============================================================
-- 说明：
-- 1. 单表与分表的物理表结构建议保持一致（字段、索引尽量一致）
-- 2. order_id 使用 BIGINT 类型，支持雪花算法生成的 64 位长整型
-- 3. 为 user_id 和 create_time 创建了索引，优化查询性能
-- 4. 不要在表中使用 AUTO_INCREMENT，主键由 ShardingSphere 的雪花算法生成
-- ============================================================
