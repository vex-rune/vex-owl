#!/bin/bash
# Vex-Owl Environment Deployment Script

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

start_all() {
    echo ""
    echo "========================================"
    echo "  Vex-Owl Environment Deployment"
    echo "========================================"
    echo ""

    echo "[1/3] Starting Nacos..."
    docker run -d --name vex-owl-nacos \
        -p 8848:8848 -p 9848:9848 -p 9849:9849 \
        -e MODE=standalone \
        -e NACOS_AUTH_ENABLE=true \
        -e NACOS_AUTH_IDENTITY_KEY=vex-owl \
        -e NACOS_AUTH_IDENTITY_VALUE=vex-owl@2026 \
        -e NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789 \
        -e JVM_XMS=768m -e JVM_XMX=768m -e JVM_XMN=384m \
        -v $SCRIPT_DIR/data/nacos/data:/home/nacos/data \
        -v $SCRIPT_DIR/data/nacos/logs:/home/nacos/logs \
        --restart unless-stopped \
        nacos/nacos-server:v2.4.3
    echo "  OK Nacos started (http://localhost:8848/nacos)"

    echo ""
    echo "[2/3] Starting Redis..."
    docker run -d --name vex-owl-redis \
        -p 6379:6379 \
        -v $SCRIPT_DIR/data/redis/data:/data \
        --restart unless-stopped \
        redis:7.2.4 \
        redis-server --appendonly yes --maxmemory 256mb --requirepass VexOwl2026@Redis
    echo "  OK Redis started (port: 6379, password: VexOwl2026@Redis)"

    echo ""
    echo "[3/3] Starting PostgreSQL..."
    docker run -d --name vex-owl-postgres \
        -p 5432:5432 \
        -e POSTGRES_DB=vex_owl \
        -e POSTGRES_USER=vex_user \
        -e POSTGRES_PASSWORD=vex_password \
        -e TZ=Asia/Shanghai \
        -v $SCRIPT_DIR/data/postgres/data:/var/lib/postgresql/data \
        --restart unless-stopped \
        postgres:15-alpine postgres \
        -c max_connections=200 -c shared_buffers=256MB -c work_mem=4MB
    echo "  OK PostgreSQL started (localhost:5432)"

    echo ""
    echo "========================================"
    echo "  Deployment Complete!"
    echo "========================================"
    echo ""
}

stop_all() {
    echo ""
    echo "========================================"
    echo "  Stopping All Services"
    echo "========================================"
    echo ""

    docker stop vex-owl-nacos 2>/dev/null; docker rm vex-owl-nacos 2>/dev/null || true
    docker stop vex-owl-redis 2>/dev/null; docker rm vex-owl-redis 2>/dev/null || true
    docker stop vex-owl-postgres 2>/dev/null; docker rm vex-owl-postgres 2>/dev/null || true

    echo "OK All services stopped"
    echo ""
}

show_status() {
    echo ""
    echo "========================================"
    echo "  Service Status"
    echo "========================================"
    echo ""

    for container in vex-owl-nacos vex-owl-redis vex-owl-postgres; do
        if docker ps --filter "name=$container" --format "{{.Status}}" | grep -q "Up"; then
            echo "[$container] RUNNING"
        else
            echo "[$container] STOPPED"
        fi
    done
    echo ""
}

case "${1:-start}" in
    start)   start_all ;;
    stop)    stop_all ;;
    restart) stop_all; start_all ;;
    status)  show_status ;;
    *)       echo "Usage: $0 {start|stop|restart|status}" ;;
esac