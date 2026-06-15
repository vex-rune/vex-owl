### 问题原因
系统**无可用 YUM 源**，先修复阿里龙蜥(Alinux)默认源，再装 Docker。

---
### 一、恢复 Alinux 官方默认 YUM 源
#### 1. 备份原有repo文件
```bash
mv /etc/yum.repos.d/*.repo /etc/yum.repos.d/bak/ 2>/dev/null || mkdir -p /etc/yum.repos.d/bak
```

#### 2. 写入龙蜥官方源（Alinux 通用）
```bash
tee /etc/yum.repos.d/alinux-base.repo << EOF
[alinux-base]
name=Alinux Base
baseurl=https://mirrors.aliyun.com/alinux/\$releasever/os/\$basearch/
gpgcheck=1
gpgkey=https://mirrors.aliyun.com/alinux/\$releasever/os/\$basearch/RPM-GPG-KEY-Alinux
enabled=1

[alinux-updates]
name=Alinux Updates
baseurl=https://mirrors.aliyun.com/alinux/\$releasever/updates/\$basearch/
gpgcheck=1
gpgkey=https://mirrors.aliyun.com/alinux/\$releasever/os/\$basearch/RPM-GPG-KEY-Alinux
enabled=1
EOF
```

#### 3. 清理并刷新缓存
```bash
yum clean all && yum makecache
```

---
### 二、继续安装 Docker
```bash
# 安装依赖
yum install -y yum-utils

# 添加 Docker 阿里源
yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

# 安装 Docker
yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动+开机自启
systemctl start docker
systemctl enable docker

# 验证
docker --version
```

---
### 三、配置镜像加速（可选）
```bash
mkdir -p /etc/docker
tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://mirror.aliyuncs.com"]
}
EOF
systemctl daemon-reload
systemctl restart docker
```

你只加了文件参数，**缺少执行指令**，补上 `up` 即可：
```bash
# 前台运行
docker compose -f vex-group/redis.yml up

# 后台守护运行（常用）
docker compose -f vex-group/redis.yml up -d
```

### 常用配套指令
```bash
# 查看日志
docker compose -f vex-group/redis.yml logs -f

# 停止并删除容器/网络
docker compose -f vex-group/redis.yml down

# 重启
docker compose -f vex-group/redis.yml restart
```

核心问题：**拉取镜像超时，境外仓库访问不通**，先配置Docker国内镜像源，再重新执行。

### 1. 配置镜像加速
```bash
# 创建配置目录
mkdir -p /etc/docker

# 写入国内镜像源
tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com"
  ]
}
EOF

# 重载配置 + 重启Docker
systemctl daemon-reload
systemctl restart docker
```

### 2. 验证镜像源生效
```bash
docker info
```
查看输出里 `Registry Mirrors` 能看到上面配置的地址即可。
