#!/bin/bash
# ================================================
# Vex-Owl Docker 完整部署与检查脚本
# 版本: 2.0.0
# ================================================
# 功能：
#   - 部署所有 Docker 服务
#   - 验证端口映射配置
#   - 测试网络连通性
#   - 验证服务功能
#   - 提供完整的错误处理和状态验证
# ================================================

set -e

# ================================================
# 配置参数
# ================================================
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly COMPOSE_FILE="${SCRIPT_DIR}/vex-group/docker-compose.yml"
readonly DOCKER_SOCKET="/var/run/docker.sock"

# 服务配置
readonly NACOS_HOST="localhost"
readonly NACOS_PORT="8848"
readonly NACOS_GRPC_PORT="9848"
readonly POSTGRES_HOST="localhost"
readonly POSTGRES_PORT="5432"
readonly POSTGRES_USER="vex_user"
readonly POSTGRES_DB="vex_owl"
readonly POSTGRES_PASSWORD="vex_password"

# 超时配置
readonly MAX_RETRIES=30
readonly RETRY_INTERVAL=5
readonly STARTUP_WAIT=60

# 颜色定义
readonly COLOR_RED='\033[0;31m'
readonly COLOR_GREEN='\033[0;32m'
readonly COLOR_YELLOW='\033[1;33m'
readonly COLOR_BLUE='\033[0;34m'
readonly COLOR_CYAN='\033[0;36m'
readonly COLOR_RESET='\033[0m'

# ================================================
# 日志函数
# ================================================
log_info() {
    echo -e "${COLOR_BLUE}[INFO]${COLOR_RESET} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_success() {
    echo -e "${COLOR_GREEN}[SUCCESS]${COLOR_RESET} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_warning() {
    echo -e "${COLOR_YELLOW}[WARNING]${COLOR_RESET} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_error() {
    echo -e "${COLOR_RED}[ERROR]${COLOR_RESET} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_section() {
    echo ""
    echo -e "${COLOR_CYAN}===============================================${COLOR_RESET}"
    echo -e "${COLOR_CYAN}  $1${COLOR_RESET}"
    echo -e "${COLOR_CYAN}===============================================${COLOR_RESET}"
}

# ================================================
# 环境检查
# ================================================
check_environment() {
    log_section "检查运行环境"

    # 检查 Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装"
        return 1
    fi
    log_success "Docker 已安装: $(docker --version)"

    # 检查 Docker Compose
    if command -v docker-compose &> /dev/null; then
        readonly DOCKER_COMPOSE_CMD="docker-compose"
        readonly COMPOSE_PULL_FLAG="--pull never"
        log_success "Docker Compose CLI 已安装"
    elif docker compose version &> /dev/null; then
        readonly DOCKER_COMPOSE_CMD="docker compose"
        readonly COMPOSE_PULL_FLAG="--no-pull"
        log_success "Docker Compose Plugin 已安装"
    else
        log_error "Docker Compose 未安装"
        return 1
    fi

    # 检查 Docker 服务
    if ! docker info &> /dev/null; then
        log_error "Docker 服务未运行，请先启动 Docker"
        return 1
    fi
    log_success "Docker 服务正在运行"

    # 检查 Compose 文件
    if [ ! -f "$COMPOSE_FILE" ]; then
        log_error "docker-compose.yml 文件不存在: $COMPOSE_FILE"
        return 1
    fi
    log_success "docker-compose.yml 文件存在"

    return 0
}

# ================================================
# 清理旧环境
# ================================================
cleanup_environment() {
    log_section "清理旧环境"

    # 停止并删除旧容器
    log_info "停止旧容器..."
    ${DOCKER_COMPOSE_CMD} -f "$COMPOSE_FILE" down --remove-orphans 2>/dev/null || true

    # 清理未使用的网络
    log_info "清理未使用的网络..."
    docker network prune -f 2>/dev/null || true

    log_success "环境清理完成"
}

# ================================================
# 部署服务
# ================================================
deploy_services() {
    log_section "部署 Docker 服务"

    log_info "启动所有服务（使用本地镜像）..."
    ${DOCKER_COMPOSE_CMD} -f "$COMPOSE_FILE" up -d ${COMPOSE_PULL_FLAG}

    log_success "服务部署完成"
}

# ================================================
# 检查容器状态
# ================================================
check_container_status() {
    log_section "检查容器状态"

    local containers=("vex-owl-nacos" "vex-owl-postgres")
    local all_running=true

    for container in "${containers[@]}"; do
        if docker ps --filter "name=${container}" --format "{{.Names}}" | grep -q "^${container}$"; then
            local status=$(docker ps --filter "name=${container}" --format "{{.Status}}")
            log_success "${container} 运行中: ${status}"
        else
            log_error "${container} 未运行"
            all_running=false
        fi
    done

    if [ "$all_running" = true ]; then
        return 0
    else
        return 1
    fi
}

# ================================================
# 验证端口映射
# ================================================
verify_port_mappings() {
    log_section "验证端口映射"

    local ports=(
        "${NACOS_PORT}:8848:Nacos HTTP"
        "${NACOS_GRPC_PORT}:9848:Nacos gRPC"
        "${POSTGRES_PORT}:5432:PostgreSQL"
    )
    local all_ports_ok=true

    for port_info in "${ports[@]}"; do
        IFS=':' read -r host_port container_port service_name <<< "$port_info"

        if docker port "vex-owl-nacos" "${container_port}" 2>/dev/null | grep -q "${host_port}"; then
            log_success "${service_name} 端口映射正确: ${host_port} -> ${container_port}"
        else
            # 使用 nc 或 telnet 测试端口
            if command -v nc &> /dev/null; then
                if nc -z "$host_port" "$host_port" 2>/dev/null; then
                    log_success "${service_name} 端口可访问: ${host_port}"
                else
                    log_warning "${service_name} 端口 ${host_port} 暂时不可访问（服务可能正在启动）"
                fi
            else
                log_warning "${service_name} 端口映射验证跳过（nc 命令不可用）"
            fi
        fi
    done

    return 0
}

# ================================================
# 测试网络连通性
# ================================================
test_network_connectivity() {
    log_section "测试网络连通性"

    local network_name="vex-owl-docker_vex-owl-network"

    # 检查网络是否存在
    if docker network ls --format "{{.Name}}" | grep -q "${network_name}"; then
        log_success "Docker 网络存在: ${network_name}"
    else
        log_warning "Docker 网络名称可能不同，尝试查找..."
        docker network ls
    fi

    # 测试容器间网络
    log_info "测试容器间网络连通性..."

    # Nacos -> PostgreSQL
    if docker exec vex-owl-nacos ping -c 1 postgres &> /dev/null; then
        log_success "Nacos 可以访问 PostgreSQL (容器间网络正常)"
    else
        log_warning "Nacos 无法通过服务名访问 PostgreSQL（可能是网络配置问题）"
    fi

    # PostgreSQL -> Nacos
    if docker exec vex-owl-postgres ping -c 1 nacos &> /dev/null; then
        log_success "PostgreSQL 可以访问 Nacos (容器间网络正常)"
    else
        log_warning "PostgreSQL 无法通过服务名访问 Nacos（可能是网络配置问题）"
    fi

    # 测试主机到容器
    log_info "测试主机到容器连通性..."

    # Nacos HTTP
    if curl -sf "http://${NACOS_HOST}:${NACOS_PORT}/nacos/v1/console/health/readiness" > /dev/null 2>&1; then
        log_success "主机可以访问 Nacos (HTTP)"
    else
        log_warning "主机无法访问 Nacos (HTTP)，服务可能正在启动"
    fi

    # PostgreSQL
    if command -v psql &> /dev/null; then
        if PGPASSWORD="${POSTGRES_PASSWORD}" psql -h "${POSTGRES_HOST}" -p "${POSTGRES_PORT}" -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -c "SELECT 1;" &> /dev/null; then
            log_success "主机可以访问 PostgreSQL"
        else
            log_warning "主机无法访问 PostgreSQL，数据库可能正在启动"
        fi
    else
        log_info "psql 命令不可用，跳过 PostgreSQL 连接测试"
    fi

    return 0
}

# ================================================
# 验证 Nacos 功能
# ================================================
verify_nacos_functionality() {
    log_section "验证 Nacos 功能"

    local retry_count=0
    local max_attempts=${MAX_RETRIES}

    log_info "等待 Nacos 服务就绪..."

    while [ $retry_count -lt $max_attempts ]; do
        if curl -sf "http://${NACOS_HOST}:${NACOS_PORT}/nacos/v1/console/health/readiness" > /dev/null 2>&1; then
            log_success "Nacos 健康检查通过 (尝试 $((retry_count + 1))/${max_attempts})"
            break
        fi

        retry_count=$((retry_count + 1))
        if [ $retry_count -lt $max_attempts ]; then
            log_info "等待 Nacos 启动... ($retry_count/$max_attempts)"
            sleep $RETRY_INTERVAL
        fi
    done

    if [ $retry_count -ge $max_attempts ]; then
        log_error "Nacos 健康检查超时"
        return 1
    fi

    # 测试 Nacos API
    log_info "测试 Nacos API 功能..."

    # 获取配置列表
    local config_response
    config_response=$(curl -s "http://${NACOS_HOST}:${NACOS_PORT}/nacos/v1/cs/configs?dataId=&group=&pageNo=1&pageSize=1" 2>/dev/null)

    if [ -n "$config_response" ]; then
        log_success "Nacos API 响应正常"

        # 测试服务注册
        local service_response
        service_response=$(curl -s "http://${NACOS_HOST}:${NACOS_PORT}/nacos/v1/ns/instance?serviceName=test&ip=127.0.0.1&port=8080" -X POST 2>/dev/null)

        if echo "$service_response" | grep -q "true\|ok"; then
            log_success "Nacos 服务注册功能正常"
        else
            log_warning "Nacos 服务注册功能测试结果不确定"
        fi
    else
        log_warning "Nacos API 响应为空"
    fi

    return 0
}

# ================================================
# 验证 PostgreSQL 功能
# ================================================
verify_postgres_functionality() {
    log_section "验证 PostgreSQL 功能"

    local retry_count=0
    local max_attempts=${MAX_RETRIES}

    log_info "等待 PostgreSQL 服务就绪..."

    while [ $retry_count -lt $max_attempts ]; do
        if docker exec vex-owl-postgres pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" &> /dev/null; then
            log_success "PostgreSQL 就绪 (尝试 $((retry_count + 1))/${max_attempts})"
            break
        fi

        retry_count=$((retry_count + 1))
        if [ $retry_count -lt $max_attempts ]; then
            log_info "等待 PostgreSQL 启动... ($retry_count/$max_attempts)"
            sleep $RETRY_INTERVAL
        fi
    done

    if [ $retry_count -ge $max_attempts ]; then
        log_error "PostgreSQL 启动超时"
        return 1
    fi

    # 测试数据库连接
    log_info "测试数据库连接..."

    local connection_test
    connection_test=$(docker exec vex-owl-postgres psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -c "SELECT version();" -t 2>/dev/null)

    if [ -n "$connection_test" ]; then
        log_success "PostgreSQL 连接成功"
        log_info "PostgreSQL 版本: $(echo "$connection_test" | head -n 1)"
    else
        log_error "PostgreSQL 连接失败"
        return 1
    fi

    # 测试数据库操作
    log_info "测试数据库操作..."

    if docker exec vex-owl-postgres psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -c "CREATE TABLE IF NOT EXISTS health_check (id SERIAL PRIMARY KEY, check_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP);" &> /dev/null; then
        log_success "数据库写入操作正常"
    else
        log_warning "数据库写入操作失败"
    fi

    if docker exec vex-owl-postgres psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -c "SELECT * FROM health_check;" &> /dev/null; then
        log_success "数据库读取操作正常"
    else
        log_warning "数据库读取操作失败"
    fi

    return 0
}

# ================================================
# 生成状态报告
# ================================================
generate_status_report() {
    log_section "生成状态报告"

    echo ""
    echo "=============================================="
    echo "         Vex-Owl Docker 服务状态报告"
    echo "=============================================="
    echo ""

    echo "容器状态:"
    echo "----------------------------------------------"
    ${DOCKER_COMPOSE_CMD} -f "$COMPOSE_FILE" ps
    echo ""

    echo "资源使用情况:"
    echo "----------------------------------------------"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}" \
        $(docker ps --filter "name=vex-owl-" --format "{{.Names}}") 2>/dev/null || echo "无法获取资源使用情况"
    echo ""

    echo "端口映射:"
    echo "----------------------------------------------"
    echo "Nacos HTTP:   http://${NACOS_HOST}:${NACOS_PORT}/nacos"
    echo "Nacos gRPC:   ${NACOS_HOST}:${NACOS_GRPC_PORT}"
    echo "PostgreSQL:   ${POSTGRES_HOST}:${POSTGRES_PORT}"
    echo ""

    echo "访问凭证:"
    echo "----------------------------------------------"
    echo "Nacos 控制台: http://${NACOS_HOST}:${NACOS_PORT}/nacos"
    echo "Nacos 账号:   nacos / nacos"
    echo ""
    echo "PostgreSQL:   ${POSTGRES_HOST}:${POSTGRES_PORT}"
    echo "数据库名:     ${POSTGRES_DB}"
    echo "用户名:       ${POSTGRES_USER}"
    echo "密码:         ${POSTGRES_PASSWORD}"
    echo ""

    echo "常用命令:"
    echo "----------------------------------------------"
    echo "查看日志:     ${DOCKER_COMPOSE_CMD} -f ${COMPOSE_FILE} logs -f"
    echo "停止服务:     ${DOCKER_COMPOSE_CMD} -f ${COMPOSE_FILE} down"
    echo "重启服务:     ${DOCKER_COMPOSE_CMD} -f ${COMPOSE_FILE} restart"
    echo "重新部署:     bash $(basename "$0")"
    echo ""
}

# ================================================
# 主函数
# ================================================
main() {
    local exit_code=0

    echo ""
    echo "=============================================="
    echo "     Vex-Owl Docker 部署与检查脚本"
    echo "=============================================="
    echo ""

    # 解析参数
    case "${1:-deploy}" in
        deploy|start)
            ACTION="deploy"
            ;;
        restart)
            ACTION="restart"
            ;;
        check|status)
            ACTION="check"
            ;;
        *)
            echo "用法: $0 {deploy|restart|check|status}"
            echo ""
            echo "命令说明:"
            echo "  deploy   - 部署并检查所有服务（默认）"
            echo "  restart  - 重启所有服务"
            echo "  check    - 仅检查服务状态"
            echo "  status   - 显示服务状态报告"
            exit 0
            ;;
    esac

    # 执行检查
    if ! check_environment; then
        log_error "环境检查失败"
        exit 1
    fi

    if [ "$ACTION" = "deploy" ]; then
        cleanup_environment
        deploy_services
    elif [ "$ACTION" = "restart" ]; then
        log_info "重启所有服务..."
        ${DOCKER_COMPOSE_CMD} -f "$COMPOSE_FILE" restart
    fi

    # 等待服务启动
    log_info "等待服务启动 (${STARTUP_WAIT}秒)..."
    sleep $STARTUP_WAIT

    # 执行各项检查
    if ! check_container_status; then
        log_error "容器状态检查失败"
        exit_code=1
    fi

    verify_port_mappings
    test_network_connectivity

    if ! verify_nacos_functionality; then
        log_error "Nacos 功能验证失败"
        exit_code=1
    fi

    if ! verify_postgres_functionality; then
        log_error "PostgreSQL 功能验证失败"
        exit_code=1
    fi

    # 生成状态报告
    generate_status_report

    # 输出最终结果
    echo "=============================================="
    if [ $exit_code -eq 0 ]; then
        log_success "所有检查通过！环境已准备就绪"
    else
        log_error "部分检查失败，请查看上述错误信息"
    fi
    echo "=============================================="
    echo ""

    exit $exit_code
}

# 执行主函数
main "$@"