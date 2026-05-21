# Vex-Owl Environment Deployment Script
param(
    [ValidateSet("start", "stop", "restart", "status", "remove")]
    [string]$Action = "start"
)

$ErrorActionPreference = "Stop"

function Write-Banner {
    param([string]$Message)
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  $Message" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
}

function Start-All {
    Write-Banner "Vex-Owl Environment Deployment"

    Write-Host "[1/3] Starting Nacos..." -ForegroundColor Yellow
    docker run -d --name vex-owl-nacos `
        -p 8848:8848 -p 9848:9848 -p 9849:9849 `
        -e MODE=standalone `
        -e NACOS_AUTH_ENABLE=true `
        -e NACOS_AUTH_IDENTITY_KEY=vex-owl `
        -e NACOS_AUTH_IDENTITY_VALUE=vex-owl@2026 `
        -e NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789 `
        -e JVM_XMS=768m -e JVM_XMX=768m -e JVM_XMN=384m `
        -v c:/work/vex/vex-owl/docker/vex-group/data/nacos/data:/home/nacos/data `
        -v c:/work/vex/vex-owl/docker/vex-group/data/nacos/logs:/home/nacos/logs `
        --restart unless-stopped `
        nacos/nacos-server:v2.4.3
    Write-Host "  OK Nacos started (http://localhost:8848/nacos)" -ForegroundColor Green

    Write-Host ""
    Write-Host "[2/3] Starting Redis..." -ForegroundColor Yellow
    docker run -d --name vex-owl-redis `
        -p 6379:6379 `
        -e REDIS_PASSWORD=VexOwl2026@Redis `
        -v c:/work/vex/vex-owl/docker/vex-group/data/redis/data:/data `
        --restart unless-stopped `
        redis:7.2.4 `
        redis-server --appendonly yes --maxmemory 256mb --requirepass VexOwl2026@Redis
    Write-Host "  OK Redis started (port: 6379, password: VexOwl2026@Redis)" -ForegroundColor Green

    Write-Host ""
    Write-Host "[3/3] Starting PostgreSQL..." -ForegroundColor Yellow
    docker run -d --name vex-owl-postgres `
        -p 5432:5432 `
        -e POSTGRES_DB=vex_owl `
        -e POSTGRES_USER=vex_user `
        -e POSTGRES_PASSWORD=vex_password `
        -e TZ=Asia/Shanghai `
        -v c:/work/vex/vex-owl/docker/vex-group/data/postgres/data:/var/lib/postgresql/data `
        --restart unless-stopped `
        postgres:15-alpine postgres `
        -c max_connections=200 -c shared_buffers=256MB -c work_mem=4MB
    Write-Host "  OK PostgreSQL started (localhost:5432)" -ForegroundColor Green

    Write-Host ""
    Write-Banner "Deployment Complete!"
}

function Stop-All {
    Write-Banner "Stopping All Services"
    docker stop vex-owl-nacos 2>$null; docker rm vex-owl-nacos 2>$null
    docker stop vex-owl-redis 2>$null; docker rm vex-owl-redis 2>$null
    docker stop vex-owl-postgres 2>$null; docker rm vex-owl-postgres 2>$null
    Write-Host "OK All services stopped" -ForegroundColor Green
}

function Show-Status {
    Write-Banner "Service Status"

    $containers = @("vex-owl-nacos", "vex-owl-redis", "vex-owl-postgres")
    foreach ($c in $containers) {
        $status = docker ps --filter "name=$c" --format "{{.Status}}" 2>$null
        if ($status) {
            Write-Host "[$c] RUNNING" -ForegroundColor Green
        } else {
            Write-Host "[$c] STOPPED" -ForegroundColor Red
        }
    }
    Write-Host ""
}

switch ($Action) {
    "start"   { Start-All }
    "stop"    { Stop-All }
    "restart" { Stop-All; Start-All }
    "status"  { Show-Status }
}

Write-Host ""