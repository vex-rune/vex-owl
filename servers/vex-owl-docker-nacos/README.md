# Docker 部署配置
## 功能描述
全项目中间件容器化配置和本地部署测试脚本目录，提供开发环境一键搭建能力，包含所有依赖中间件的容器编排配置和本地测试辅助脚本
## 目录结构
```
docker/
├── middleware/           # 各中间件部署配置
│   ├── nacos/
│   ├── postgresql/
│   └── nginx/
├── env/                  # 环境变量配置模板
│   └── .env.dev.example
├── scripts/              # 本地开发测试辅助脚本
│   ├── start-dev.sh      # 一键启动本地开发环境
│   ├── stop-dev.sh       # 停止本地环境
│   ├── init-db.sh        # 数据库初始化脚本
│   └── backup-db.sh      # 数据备份脚本
└── docker-compose.dev.yml # 本地开发环境编排文件
```
## 核心功能
- 🚀 **本地环境一键搭建**：一条命令启动所有依赖中间件，快速准备开发环境
- 🔧 **开发配置统一**：所有开发者使用相同的中间件配置，避免环境差异问题
- 💾 **开发数据持久化**：数据库数据本地持久化，重启服务不丢失测试数据
- 🧪 **测试环境预置**：预置测试账号、测试数据，方便本地开发调试
- 📋 **辅助脚本集合**：提供数据库初始化、备份、环境清理等常用脚本
## 本地环境要求
- 本地开发机器配置：2核4G及以上
- Docker Desktop >= 4.0.0 (Windows/Mac) 或 Docker Engine >= 20.10.0 (Linux)
- Docker Compose >= 2.0.0
## 常用命令
```bash
# 一键启动本地开发所有中间件
docker-compose -f docker-compose.dev.yml up -d
# 停止本地开发环境
docker-compose -f docker-compose.dev.yml down
# 查看中间件运行状态
docker-compose -f docker-compose.dev.yml ps
# 查看指定中间件日志
docker-compose -f docker-compose.dev.yml logs -f [中间件名]
# 初始化数据库（首次启动后执行）
sh scripts/init-db.sh
# 备份本地数据库数据
sh scripts/backup-db.sh
# 查看Docker资源占用
docker stats
```
## 服务端口映射
| 服务 | 宿主机端口 | 容器端口 | 说明 |
|------|-----------|---------|------|
| Nginx | 80 | 80 | Web前端入口 |
| Gateway | 8080 | 8080 | API网关入口 |
| Nacos | 8848 | 8848 | Nacos控制台 |
| PostgreSQL | 5432 | 5432 | 数据库端口 |
