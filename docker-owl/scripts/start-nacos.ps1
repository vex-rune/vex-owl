# Nacos 启动脚本（Windows PowerShell版本）
<#
脚本功能：一键启动Nacos服务，自动创建数据目录、检查环境，启动成功后输出访问信息
使用方法：在PowerShell中直接运行 ./start-nacos.ps1 即可
#>
# 输出启动提示，ForegroundColor指定文字颜色，提升可读性
Write-Host "🚀 正在启动 Nacos 服务..." -ForegroundColor Green
<#
切换到脚本所在目录：
$PSScriptRoot 是PowerShell内置变量，表示当前脚本所在的目录
这样无论在哪个目录执行脚本，都能正确找到docker-compose配置文件
#>
Set-Location $PSScriptRoot
# 切换到docker目录（脚本在scripts目录，docker-compose在父级目录）
Set-Location ..
<#
检查Docker是否正在运行：
docker info 命令会返回Docker的运行信息，如果Docker未启动会执行失败
2>&1 表示把错误输出重定向到标准输出，避免终端显示错误信息
$LASTEXITCODE 是PowerShell内置变量，表示上一条命令的退出码，0表示成功，非0表示失败
#>
$dockerRunning = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker 未运行，请先启动 Docker Desktop" -ForegroundColor Red
    exit 1
}
<#
自动创建Nacos数据目录：
检查C:/data/owl/nacos下的三个子目录是否存在，不存在则自动创建
-Force 参数会自动创建多级父目录，即使目录已存在也不会报错
Out-Null 表示隐藏创建目录的输出信息，保持终端整洁
#>
Write-Host "📂 正在检查并创建Nacos数据目录..." -ForegroundColor Cyan
$nacosDirs = @(
    "C:/data/owl/nacos/conf",
    "C:/data/owl/nacos/data", 
    "C:/data/owl/nacos/logs"
)
foreach ($dir in $nacosDirs) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "  ✅ 创建目录: $dir" -ForegroundColor Green
    }
}
<#
启动Nacos服务：
-f 参数指定使用的docker-compose配置文件
up -d 表示后台启动容器（detached模式）
-p vex-owl 指定项目前缀，Docker Desktop中分组显示为vex-owl，更清晰
#>
docker-compose -p vex-owl -f docker-compose.nacos.yml up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Nacos 启动失败，请检查上面的错误信息" -ForegroundColor Red
    exit 1
}
# 启动成功提示
Write-Host "✅ Nacos 启动成功！" -ForegroundColor Green
# 输出访问信息
Write-Host "`n📋 访问信息：" -ForegroundColor Yellow
Write-Host "  控制台地址: http://localhost:8848/nacos"
Write-Host "  默认用户名: nacos"
Write-Host "  默认密码: nacos"
# 温馨提示，Nacos启动需要时间，避免用户立即访问出错
Write-Host "`n⌛ 请等待1-2分钟待服务完全启动后再访问" -ForegroundColor Cyan
# 输出常用调试命令
Write-Host "`n🔍 常用命令：" -ForegroundColor Gray
Write-Host "  查看实时日志: docker-compose -p vex-owl -f docker-compose.nacos.yml logs -f nacos"
Write-Host "  查看运行状态: docker-compose -p vex-owl -f docker-compose.nacos.yml ps"
Write-Host "  停止Nacos服务: ./stop-nacos.ps1"
