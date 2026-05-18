<#
.SYNOPSIS
一键启动Nacos服务脚本，适用于Windows PowerShell环境
#>

Write-Host "===================================" -ForegroundColor Cyan
Write-Host "Vex-Owl Nacos 启动脚本" -ForegroundColor Green
Write-Host "===================================" -ForegroundColor Cyan

# 检查docker是否安装
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "错误：未检测到Docker，请先安装Docker并启动" -ForegroundColor Red
    exit 1
}

# 检查docker-compose是否安装
if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Host "错误：未检测到Docker Compose，请先安装" -ForegroundColor Red
    exit 1
}

# 切换到脚本所在目录
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location "$scriptPath/.."

Write-Host "当前工作目录：$(Get-Location)" -ForegroundColor Yellow

# 检查docker-compose文件是否存在
if (-not (Test-Path "docker-compose.nacos.yml")) {
    Write-Host "错误：docker-compose.nacos.yml 配置文件不存在" -ForegroundColor Red
    exit 1
}

# 启动Nacos
Write-Host "正在启动Nacos服务..." -ForegroundColor Green
docker-compose -f docker-compose.nacos.yml up -d

# 检查启动结果
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Nacos服务启动成功！" -ForegroundColor Green
    Write-Host "🌐 控制台访问地址：http://localhost:8848/nacos" -ForegroundColor Cyan
    Write-Host "🔑 默认账号密码：nacos/nacos" -ForegroundColor Yellow
    Write-Host "⏳ 请等待30秒左右待Nacos完全启动后再使用" -ForegroundColor Gray
} else {
    Write-Host "❌ Nacos服务启动失败，请查看上面的错误日志" -ForegroundColor Red
    exit 1
}
