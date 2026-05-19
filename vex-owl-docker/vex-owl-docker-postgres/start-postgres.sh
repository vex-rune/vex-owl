#!/bin/bash

# Vex Owl PostgreSQL Docker 启动脚本
# 用于在 Linux/Mac 环境下启动 PostgreSQL 服务

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.postgres.yml"

echo "========================================="
echo "  Vex Owl PostgreSQL Docker 启动脚本"
echo "========================================="
echo ""

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ 错误: Docker 未安装"
    echo "请先安装 Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ 错误: Docker Compose 未安装"
    echo "请先安装 Docker Compose: https://docs.docker.com/compose/install/"
    exit 1
fi

# 确定使用的 Docker Compose 命令
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

echo "✅ Docker 和 Docker Compose 已就绪"
echo ""

# 创建初始化脚本目录
if [ ! -d "${SCRIPT_DIR}/init-scripts" ]; then
    echo "📁 创建初始化脚本目录..."
    mkdir -p "${SCRIPT_DIR}/init-scripts"
    
    # 创建示例初始化脚本
    cat > "${SCRIPT_DIR}/init-scripts/01-init.sql" << 'EOF'
-- 创建扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- 显示欢迎信息
SELECT 'Vex Owl PostgreSQL 数据库初始化完成！' AS message;
EOF
    
    echo "✅ 已创建示例初始化脚本: init-scripts/01-init.sql"
fi

# 解析命令行参数
ACTION=${1:-start}

case $ACTION in
    start)
        echo "🚀 启动 PostgreSQL 服务..."
        echo ""
        $DOCKER_COMPOSE -f "$COMPOSE_FILE" up -d
        
        echo ""
        echo "⏳ 等待 PostgreSQL 启动..."
        sleep 5
        
        # 检查服务状态
        if $DOCKER_COMPOSE -f "$COMPOSE_FILE" ps | grep -q "postgres.*Up"; then
            echo ""
            echo "✅ PostgreSQL 启动成功！"
            echo ""
            echo "📊 连接信息："
            echo "   主机: localhost"
            echo "   端口: 5432"
            echo "   数据库: vex_owl"
            echo "   用户名: vex_user"
            echo "   密码: vex_password"
            echo ""
            echo "📝 常用命令："
            echo "   查看日志: $DOCKER_COMPOSE -f $COMPOSE_FILE logs -f postgres"
            echo "   进入容器: docker exec -it vex-postgres psql -U vex_user -d vex_owl"
            echo "   停止服务: $DOCKER_COMPOSE -f $COMPOSE_FILE down"
            echo ""
        else
            echo ""
            echo "❌ PostgreSQL 启动失败，请查看日志："
            echo "   $DOCKER_COMPOSE -f $COMPOSE_FILE logs postgres"
            exit 1
        fi
        ;;
    
    stop)
        echo "🛑 停止 PostgreSQL 服务..."
        $DOCKER_COMPOSE -f "$COMPOSE_FILE" down
        echo "✅ 服务已停止"
        ;;
    
    restart)
        echo "🔄 重启 PostgreSQL 服务..."
        $DOCKER_COMPOSE -f "$COMPOSE_FILE" restart
        echo "✅ 服务已重启"
        ;;
    
    status)
        echo "📊 服务状态："
        echo ""
        $DOCKER_COMPOSE -f "$COMPOSE_FILE" ps
        ;;
    
    logs)
        echo "📋 查看日志（Ctrl+C 退出）："
        echo ""
        $DOCKER_COMPOSE -f "$COMPOSE_FILE" logs -f ${2:-postgres}
        ;;
    
    clean)
        echo "⚠️  警告: 这将删除所有数据！"
        read -p "确认删除？(yes/no): " confirm
        if [ "$confirm" = "yes" ]; then
            echo "🗑️  清理服务和数据..."
            $DOCKER_COMPOSE -f "$COMPOSE_FILE" down -v
            echo "✅ 清理完成"
        else
            echo "❌ 操作已取消"
        fi
        ;;
    
    *)
        echo "用法: $0 {start|stop|restart|status|logs|clean}"
        echo ""
        echo "命令说明："
        echo "  start   - 启动服务（默认）"
        echo "  stop    - 停止服务"
        echo "  restart - 重启服务"
        echo "  status  - 查看服务状态"
        echo "  logs    - 查看日志"
        echo "  clean   - 清理服务和数据"
        exit 1
        ;;
esac
