#!/bin/bash
# ================================================
# Vex-Owl 环境部署脚本
# ================================================
# 支持：启动、停止、重启、查看状态
# 使用独立的 docker-compose 配置文件
# ================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

start_all() {
    echo ""
    echo "========================================"
    echo "  Vex-Owl Environment Deployment"
    echo "========================================"
    echo ""

    echo "[1/4] Starting Nacos..."
    docker-compose -f "$SCRIPT_DIR/vex-group/nacos.yml" up -d
    echo "  OK Nacos started (http://localhost:17696/nacos)"

    echo ""
    echo "[2/4] Starting Redis..."
    docker-compose -f "$SCRIPT_DIR/vex-group/redis.yml" up -d
    echo "  OK Redis started (localhost:12758, password: VexOwl2026@Redis)"

    echo ""
    echo "[3/4] Starting PostgreSQL..."
    docker-compose -f "$SCRIPT_DIR/vex-group/postgres.yml" up -d
    echo "  OK PostgreSQL started (localhost:10864)"

    echo ""
    echo "[4/4] Starting Mosquitto..."
    docker-compose -f "$SCRIPT_DIR/vex-group/mosquitto.yml" up -d
    echo "  OK Mosquitto started (localhost:3766 MQTT, localhost:18002 WebSocket)"

    echo ""
    echo "========================================"
    echo "  Deployment Complete!"
    echo "========================================"
    echo "服务地址："
    echo "  - Nacos:     http://localhost:17696/nacos (nacos/nacos)"
    echo "  - Redis:     localhost:12758 (password: VexOwl2026@Redis)"
    echo "  - Postgres:  localhost:10864 (vex_owl/vex_user/vex_password)"
    echo "  - Mosquitto: localhost:3766 (MQTT) / localhost:18002 (WebSocket)"
    echo ""
}

stop_all() {
    echo ""
    echo "========================================"
    echo "  Stopping All Services"
    echo "========================================"
    echo ""

    echo "Stopping Nacos..."
    docker-compose -f "$SCRIPT_DIR/vex-group/nacos.yml" down 2>/dev/null || true

    echo "Stopping Redis..."
    docker-compose -f "$SCRIPT_DIR/vex-group/redis.yml" down 2>/dev/null || true

    echo "Stopping PostgreSQL..."
    docker-compose -f "$SCRIPT_DIR/vex-group/postgres.yml" down 2>/dev/null || true

    echo "Stopping Mosquitto..."
    docker-compose -f "$SCRIPT_DIR/vex-group/mosquitto.yml" down 2>/dev/null || true

    echo ""
    echo "OK All services stopped"
    echo ""
}

show_status() {
    echo ""
    echo "========================================"
    echo "  Service Status"
    echo "========================================"
    echo ""

    for container in vex-owl-nacos vex-owl-redis vex-owl-postgres vex-owl-mosquitto; do
        if docker ps --filter "name=$container" --format "{{.Status}}" | grep -q "Up"; then
            echo "[$container] RUNNING"
        else
            echo "[$container] STOPPED"
        fi
    done
    echo ""
}

case "${1:-start}" in
    start)
        start_all
        ;;
    stop)
        stop_all
        ;;
    restart)
        stop_all
        start_all
        ;;
    status)
        show_status
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac