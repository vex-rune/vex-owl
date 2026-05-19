-- 创建扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- 显示欢迎信息
SELECT 'Vex Owl PostgreSQL 数据库初始化完成！' AS message;
