#!/bin/bash
# Nacos 启动脚本（Linux/Mac版本）
# 脚本功能：一键启动Nacos服务，自动检查环境，启动成功后输出访问信息
# 使用方法：在终端中直接运行 ./start-nacos.sh 即可，首次运行需要加执行权限：chmod +x start-nacos.sh
# \033[xxm 是ANSI颜色码，用于在终端中输出彩色文字，提升可读性
echo -e "\033[32m🚀 正在启动 Nacos 服务...\033[0m"
# 切换到脚本所在目录：
# $0 表示当前脚本的路径
# dirname "$0" 获取脚本所在的目录
# 这样无论在哪个目录执行脚本，都能正确找到docker-compose配置文件
cd "$(dirname "$0")"
# 切换到docker目录（脚本在scripts目录，docker-compose配置文件在scripts的父级目录）
cd ..
# 检查Docker是否正在运行：
# docker info 命令会返回Docker的运行信息，如果Docker未启动会执行失败
# > /dev/null 2>&1 表示把所有输出（正常输出和错误输出）都重定向到/dev/null，不显示在终端
# ! 表示取反，如果docker info执行失败（返回非0），就进入if逻辑
if ! docker info > /dev/null 2>&1; then
    echo -e "\033[31m❌ Docker 未运行，请先启动 Docker\033[0m"
    exit 1
fi
# 自动创建Nacos数据目录：
# 检查C:/data/owl/nacos下的三个子目录是否存在，不存在则自动创建
# -p 参数表示自动创建多级父目录，即使目录已存在也不会报错
echo -e "\033[36m📂 正在检查并创建Nacos数据目录...\033[0m"
# Windows/WSL环境路径写法
mkdir -p /mnt/c/data/owl/nacos/conf
mkdir -p /mnt/c/data/owl/nacos/data
mkdir -p /mnt/c/data/owl/nacos/logs
# 直接Windows路径写法，Git Bash等终端支持
mkdir -p C:/data/owl/nacos/conf
mkdir -p C:/data/owl/nacos/data
mkdir -p C:/data/owl/nacos/logs
echo -e "  \033[32m✅ 数据目录创建完成\033[0m"
# 启动Nacos服务：
# -f 参数指定使用的docker-compose配置文件，这里指定使用专门的nacos配置文件
# up -d 表示后台启动容器（detached模式），容器在后台运行不会占用当前终端
docker-compose -f docker-compose.nacos.yml up -d
# $? 是shell内置变量，表示上一条命令的退出码，0表示成功，非0表示失败
if [ $? -ne 0 ]; then
    echo -e "\033[31m❌ Nacos 启动失败，请检查上面的错误信息\033[0m"
    exit 1
fi
# 启动成功提示
echo -e "\033[32m✅ Nacos 启动成功！\033[0m"
# 输出访问信息
echo -e "\n\033[33m📋 访问信息：\033[0m"
echo "  控制台地址: http://localhost:8848/nacos"
echo "  默认用户名: nacos"
echo "  默认密码: nacos"
# 温馨提示：Nacos是Java应用，启动需要初始化Spring上下文、加载配置等，首次启动可能需要1-2分钟
echo -e "\n\033[36m⌛ 请等待1-2分钟待服务完全启动后再访问\033[0m"
# 输出常用调试命令，方便用户后续操作
echo -e "\n\033[90m🔍 常用命令：\033[0m"
echo "  查看实时日志: docker-compose -f docker-compose.nacos.yml logs -f nacos"
echo "  查看运行状态: docker-compose -f docker-compose.nacos.yml ps"
echo "  停止Nacos服务: ./stop-nacos.sh"
