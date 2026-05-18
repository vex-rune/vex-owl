#!/bin/bash
# 一键启动Nacos服务脚本，适用于Linux/MacOS环境

echo -e "\e[36m===================================\e[0m"
echo -e "\e[32mVex-Owl Nacos 启动脚本\e[0m"
echo -e "\e[36m===================================\e[0m"

# 检查docker是否安装
if ! command -v docker &> /dev/null; then
    echo -e "\e[31m错误：未检测到Docker，请先安装Docker并启动\e[0m"
    exit 1
fi

# 检查docker-compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo -e "\e[31m错误：未检测到Docker Compose，请先安装\e[0m"
    exit 1
fi

# 切换到脚本所在目录
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)
cd "$SCRIPT_DIR/.."

echo -e "\e[33m当前工作目录：$(pwd)\e[0m"

# 检查docker-compose文件是否存在
if [ ! -f "docker-compose.nacos.yml" ]; then
    echo -e "\e[31m错误：docker-compose.nacos.yml 配置文件不存在\e[0m"
    exit 1
fi

# 启动Nacos
echo -e "\e[32m正在启动Nacos服务...\e[0m"
docker-compose -f docker-compose.nacos.yml up -d

# 检查启动结果
if [ $? -eq 0 ]; then
    echo -e "\e[32m✅ Nacos服务启动成功！\e[0m"
    echo -e "\e[36m🌐 控制台访问地址：http://localhost:8848/nacos\e[0m"
    echo -e "\e[33m🔑 默认账号密码：nacos/nacos\e[0m"
    echo -e "\e[90m⏳ 请等待30秒左右待Nacos完全启动后再使用\e[0m"
else
    echo -e "\e[31m❌ Nacos服务启动失败，请查看上面的错误日志\e[0m"
    exit 1
fi
