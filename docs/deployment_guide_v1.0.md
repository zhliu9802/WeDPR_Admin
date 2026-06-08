# WeDPR 管理端部署指南（从零开始）

> 本文档面向**完全零基础**的读者，手把手说明：如何在本地电脑**构建**管理端，以及如何在**另一台服务器**上**部署运行**。
>
> 阅读本文档前，你只需要知道：管理端是一个 Web 管理后台，用来查看各机构的数据集、项目、任务等信息。

---

## 目录

1. [管理端是什么？](#1-管理端是什么)
2. [部署前你需要知道的事](#2-部署前你需要知道的事)
3. [整体流程一览](#3-整体流程一览)
4. [第一步：准备构建机（本地电脑）](#4-第一步准备构建机本地电脑)
5. [第二步：在本地构建管理端](#5-第二步在本地构建管理端)
6. [第三步：打包要上传到服务器的文件](#6-第三步打包要上传到服务器的文件)
7. [第四步：准备目标服务器](#7-第四步准备目标服务器)
8. [第五步：在服务器上安装 MySQL](#8-第五步在服务器上安装-mysql)
9. [第六步：修改配置文件](#9-第六步修改配置文件)
10. [第七步：启动管理端后端](#10-第七步启动管理端后端)
11. [第八步：部署管理端前端（Nginx）](#11-第八步部署管理端前端nginx)
12. [第九步：创建管理员账号并登录](#12-第九步创建管理员账号并登录)
13. [第十步：验证部署是否成功](#13-第十步验证部署是否成功)
14. [日常运维命令](#14-日常运维命令)
15. [常见问题](#15-常见问题)
16. [附录](#16-附录)

---

## 1. 管理端是什么？

WeDPR 管理端（`WeDPR-admin`）包含两个部分：

| 组件 | 目录 | 作用 | 默认端口 |
|------|------|------|---------|
| **后端** | `wedpr-admin/` | 提供 API 接口，汇聚各机构数据 | **6850** |
| **前端** | `wedpr-web-admin/` | 浏览器访问的管理界面 | 开发时 **3001**，生产由 Nginx 提供 |

管理端**不需要**安装站点端（`wedpr-site`），但运行时需要能连通：

- **MySQL 数据库**（存管理端自己的数据）
- **FISCO BCOS 区块链节点**（同步数据集等元数据）
- **各机构 Gateway**（接收站点端上报的项目/任务数据）

---

## 2. 部署前你需要知道的事

### 2.1 能不能只拷贝 `WeDPR-admin` 目录？

| 场景 | 是否可以 |
|------|---------|
| **在服务器上重新编译源码** | ❌ 不行。编译时还需要同级的 `WeDPR/` 目录（共享组件） |
| **拷贝已构建好的产物部署** | ✅ 可以。这是**生产环境推荐方式** |

### 2.2 源码目录结构要求（仅构建时需要）

如果你在本地**从源码编译**，目录必须是这样：

```
某个目录/
├── WeDPR/              ← 站点端仓库（提供共享 Java 组件）
└── WeDPR-admin/        ← 管理端仓库（与 WeDPR 同级）
```

> 如果你是从 Git 克隆的整个 `WeDPR` 大仓库，通常已经是这个结构。

### 2.3 需要开放的端口

| 端口 | 用途 | 是否必须对外开放 |
|------|------|----------------|
| 6850 | 管理端后端 API | 生产环境建议**不**直接对外，由 Nginx 反向代理 |
| 80 / 443 | Nginx（前端页面） | ✅ 对外开放 |
| 3306 | MySQL | 建议仅内网访问 |
| 6001 | Transport 监听 | 内网，需能被各机构 Gateway 访问 |
| 30202 等 | FISCO BCOS 节点 | 内网连通即可 |

---

## 3. 整体流程一览

```
┌─────────────────────────────────────────────────────────────┐
│  构建机（你的开发电脑 / CI 服务器）                            │
│                                                             │
│  1. 安装 JDK、Node.js、Gradle 依赖环境                       │
│  2. 编译后端 → 生成 wedpr-admin/dist/                        │
│  3. 编译前端 → 生成 wedpr-web-admin/manage/                  │
│  4. 打包 dist + manage + DDL + 配置文件                      │
└──────────────────────────┬──────────────────────────────────┘
                           │  scp / rsync 上传
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  目标服务器（生产环境）                                       │
│                                                             │
│  1. 安装 JDK、MySQL、Nginx                                   │
│  2. 初始化数据库（执行 DDL + DML）                            │
│  3. 修改 conf/ 下的配置文件                                  │
│  4. 启动后端 ./start.sh                                      │
│  5. 配置 Nginx 托管前端 + 反向代理 API                        │
│  6. 浏览器访问，用 agency_admin 账号登录                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. 第一步：准备构建机（本地电脑）

构建机是你用来**编译代码**的电脑，可以是开发机，也可以是 CI 服务器。

### 4.1 需要安装的软件

| 软件 | 版本要求 | 用途 |
|------|---------|------|
| **JDK** | 8 或以上（推荐 JDK 8 或 11） | 编译和运行 Java 后端 |
| **Node.js** | 16.x 或 18.x（LTS） | 编译 Vue 前端 |
| **npm** | 随 Node.js 自带 | 安装前端依赖 |
| **Git** | 任意较新版本 | 拉取代码（如需要） |

> Gradle 构建工具已包含在项目中（`gradlew` 脚本），**不需要**单独安装 Gradle。

### 4.2 Ubuntu / Debian 安装命令

```bash
# 更新软件源
sudo apt update

# 安装 JDK 11
sudo apt install -y openjdk-11-jdk

# 验证 Java
java -version
# 应看到类似：openjdk version "11.0.x"

# 设置 JAVA_HOME（添加到 ~/.bashrc 永久生效）
echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# 安装 Node.js 18（通过 NodeSource）
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# 验证 Node.js 和 npm
node -v    # 应显示 v18.x.x
npm -v     # 应显示 9.x 或 10.x

# 安装 Git（如果没有）
sudo apt install -y git
```

### 4.3 CentOS / RHEL 安装命令

```bash
# 安装 JDK 11
sudo yum install -y java-11-openjdk java-11-openjdk-devel

# 设置 JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk' >> ~/.bashrc
source ~/.bashrc

# 安装 Node.js 18
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install -y nodejs

# 验证
java -version
node -v
npm -v
```

### 4.4 macOS 安装命令

```bash
# 安装 Homebrew（如果没有）
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装 JDK 11
brew install openjdk@11
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 11)' >> ~/.zshrc
source ~/.zshrc

# 安装 Node.js
brew install node@18
echo 'export PATH="/opt/homebrew/opt/node@18/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# 验证
java -version
node -v
```

---

## 5. 第二步：在本地构建管理端

### 5.1 确认目录结构

```bash
# 进入你的工作目录，确认两个目录同级存在
ls -d WeDPR WeDPR-admin
# 应同时输出：
# WeDPR
# WeDPR-admin
```

如果缺少 `WeDPR` 目录，管理端**无法编译**，需要先获取站点端代码。

### 5.2 构建后端（Java）

```bash
cd WeDPR-admin

# 赋予 gradlew 执行权限（首次需要）
chmod +x gradlew

# 编译后端，跳过测试以加快速度
./gradlew :wedpr-admin:jar -x test
```

**构建成功的标志：**

- 终端最后显示 `BUILD SUCCESSFUL`
- 生成目录 `wedpr-admin/dist/`，其中包含：
  - `apps/` — 主程序 jar
  - `lib/` — 依赖库（约 400 个 jar）
  - `conf/` — 配置文件
  - `start.sh` / `stop.sh` — 启停脚本

```bash
# 检查构建产物
ls wedpr-admin/dist/
# 应看到：apps  conf  lib  start.sh  stop.sh
```

### 5.3 构建前端（Vue）

```bash
cd WeDPR-admin/wedpr-web-admin

# 安装依赖（首次需要，可能需要几分钟）
npm install

# 生产环境构建
npm run build:pro
```

**构建成功的标志：**

- 终端显示 `Build complete`
- 生成目录 `manage/`，其中包含 `index.html` 和 `static/` 等静态文件

```bash
# 检查前端产物
ls manage/
# 应看到：index.html  static/  favicon.ico 等
```

### 5.4 本地开发调试（可选）

如果你只是想在本机**试运行**，不需要部署到服务器：

```bash
# 终端 1：启动后端
cd WeDPR-admin/wedpr-admin/dist
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64   # 按实际路径修改
./start.sh

# 终端 2：启动前端开发服务器
cd WeDPR-admin/wedpr-web-admin
npm run serve
# 浏览器访问 http://localhost:3001
```

---

## 6. 第三步：打包要上传到服务器的文件

在构建机上执行：

```bash
# 假设当前在 WeDPR-admin 目录
cd /path/to/WeDPR-admin

# 创建打包目录
mkdir -p /tmp/wedpr-admin-deploy

# 1. 拷贝后端部署包
cp -r wedpr-admin/dist /tmp/wedpr-admin-deploy/backend

# 2. 拷贝前端静态文件
cp -r wedpr-web-admin/manage /tmp/wedpr-admin-deploy/frontend

# 3. 拷贝数据库初始化脚本（在 WeDPR 仓库中）
cp ../WeDPR/wedpr-builder/db/wedpr_ddl.sql /tmp/wedpr-admin-deploy/
cp ../WeDPR/wedpr-builder/db/wedpr_dml.sql /tmp/wedpr-admin-deploy/

# 4. 打成压缩包
cd /tmp
tar czvf wedpr-admin-deploy.tar.gz wedpr-admin-deploy/

echo "打包完成：/tmp/wedpr-admin-deploy.tar.gz"
ls -lh /tmp/wedpr-admin-deploy.tar.gz
```

### 打包后的目录结构

```
wedpr-admin-deploy/
├── backend/                  # 后端，直接部署
│   ├── apps/
│   ├── lib/
│   ├── conf/                 # ⚠️ 部署前必须修改
│   ├── start.sh
│   └── stop.sh
├── frontend/                 # 前端静态文件，交给 Nginx
│   ├── index.html
│   └── static/
├── wedpr_ddl.sql             # 建表脚本
└── wedpr_dml.sql             # 初始数据脚本
```

### 上传到目标服务器

```bash
# 将压缩包上传到服务器（把 user 和 server-ip 换成你的）
scp /tmp/wedpr-admin-deploy.tar.gz user@server-ip:/opt/

# SSH 登录服务器后解压
ssh user@server-ip
cd /opt
sudo tar xzvf wedpr-admin-deploy.tar.gz
sudo mv wedpr-admin-deploy /data/app/wedpr-admin
```

---

## 7. 第四步：准备目标服务器

目标服务器是**生产环境**运行管理端的机器。

### 7.1 服务器最低配置建议

| 项目 | 建议值 |
|------|--------|
| CPU | 2 核及以上 |
| 内存 | 4 GB 及以上（后端默认占用约 256 MB 堆内存） |
| 磁盘 | 20 GB 及以上 |
| 操作系统 | Ubuntu 20.04/22.04、CentOS 7/8 |

### 7.2 安装 JDK

```bash
# Ubuntu
sudo apt update
sudo apt install -y openjdk-11-jdk

# 设置环境变量
echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64' | sudo tee -a /etc/profile.d/wedpr.sh
echo 'export PATH=$JAVA_HOME/bin:$PATH' | sudo tee -a /etc/profile.d/wedpr.sh
source /etc/profile.d/wedpr.sh

# 验证
java -version
```

### 7.3 安装 Nginx

```bash
# Ubuntu
sudo apt install -y nginx

# CentOS
sudo yum install -y nginx

# 启动并设置开机自启
sudo systemctl start nginx
sudo systemctl enable nginx

# 验证（应看到 Nginx 欢迎页）
curl http://localhost
```

### 7.4 安装网络工具（排障用）

```bash
# Ubuntu
sudo apt install -y curl wget net-tools telnet unzip openssl

# CentOS
sudo yum install -y curl wget net-tools telnet unzip openssl
```

---

## 8. 第五步：在服务器上安装 MySQL

管理端需要独立的 MySQL 数据库。

### 8.1 安装 MySQL 8

```bash
# Ubuntu 22.04
sudo apt update
sudo apt install -y mysql-server

# 启动 MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# 查看状态（应显示 active (running)）
sudo systemctl status mysql
```

### 8.2 安全配置（设置 root 密码）

```bash
sudo mysql_secure_installation
```

按提示操作：
- 设置 root 密码（请记住这个密码）
- 移除匿名用户：Yes
- 禁止 root 远程登录：Yes（推荐）
- 移除 test 数据库：Yes

### 8.3 创建数据库和用户

```bash
# 登录 MySQL（使用你设置的 root 密码）
sudo mysql -u root -p
```

在 MySQL 命令行中执行：

```sql
-- 创建数据库
CREATE DATABASE wedpr3 CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

-- 创建专用用户（把 your_password 换成强密码）
CREATE USER 'wedpr_admin'@'localhost' IDENTIFIED BY 'your_password';
CREATE USER 'wedpr_admin'@'127.0.0.1' IDENTIFIED BY 'your_password';

-- 授权
GRANT ALL PRIVILEGES ON wedpr3.* TO 'wedpr_admin'@'localhost';
GRANT ALL PRIVILEGES ON wedpr3.* TO 'wedpr_admin'@'127.0.0.1';
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

### 8.4 执行建表和初始数据脚本

```bash
cd /data/app/wedpr-admin

# 建表
mysql -u wedpr_admin -p wedpr3 < wedpr_ddl.sql

# 初始数据（算法模板、默认用户组等）
mysql -u wedpr_admin -p wedpr3 < wedpr_dml.sql

# 验证表是否创建成功
mysql -u wedpr_admin -p wedpr3 -e "SHOW TABLES;"
# 应看到 wedpr_agency、wedpr_user、wedpr_config_table 等表
```

---

## 9. 第六步：修改配置文件

所有配置文件在 `backend/conf/` 目录下。**部署前必须修改**，否则服务无法正常启动。

```bash
cd /data/app/wedpr-admin/backend/conf
ls -la
# 主要文件：
# application.properties    — 服务端口、证书路径
# application-wedpr.properties — Spring 应用配置
# wedpr.properties        — 数据库、区块链、Transport（最重要）
# config.toml             — FISCO BCOS 链连接配置
```

### 9.1 修改 `wedpr.properties`（最重要）

用编辑器打开：

```bash
vi /data/app/wedpr-admin/backend/conf/wedpr.properties
# 或使用 nano：
# nano /data/app/wedpr-admin/backend/conf/wedpr.properties
```

需要修改的配置项：

```properties
# ========== 数据库配置 ==========
# 把 127.0.0.1 换成你的 MySQL 地址，wedpr3 是数据库名
wedpr.mybatis.url=jdbc:mysql://127.0.0.1/wedpr3?characterEncoding=UTF-8&allowMultiQueries=true
# 填入第 8.3 步创建的用户名和密码
wedpr.mybatis.username=wedpr_admin
wedpr.mybatis.password=your_password

# ========== 区块链配置 ==========
# 与各站点端保持完全一致！
wedpr.chain.group_id=group0
wedpr.chain.config_path=config.toml

# 合约地址（部署 wedpr-sol 合约后获得，各站点端必须相同）
wedpr.sync.recorder.factory.contract_address=0x你的合约地址
wedpr.sync.sequencer.contract_address=0x你的合约地址

# ========== Transport 配置 ==========
# 各机构 Gateway 地址，多个用逗号分隔
# 格式：ipv4:IP地址:端口
wedpr.transport.gateway_targets=ipv4:192.168.1.100:45600,ipv4:192.168.1.101:45600

# 管理端 Transport 监听（一般保持默认即可）
wedpr.transport.listen_ip=0.0.0.0
wedpr.transport.listen_port=6001
```

> **重要提示：**
> - `wedpr.sync.*.contract_address` 必须与所有站点端配置**完全相同**
> - `wedpr.transport.gateway_targets` 必须填写**真实可达**的各机构 Gateway 地址
> - 如果暂时没有区块链和 Gateway，可以先填占位值让服务启动，但管理端无法看到站点数据

### 9.2 修改 `config.toml`（区块链连接）

```bash
vi /data/app/wedpr-admin/backend/conf/config.toml
```

```toml
[cryptoMaterial]
certPath = "conf"
disableSsl = "false"
useSMCrypto = "false"

[network]
messageTimeout = "10000"
defaultGroup = "group0"
# 改成你的 FISCO BCOS 节点地址
peers = ["192.168.1.50:30202", "192.168.1.50:30203"]
```

### 9.3 配置区块链证书

FISCO BCOS 需要 SSL 证书才能连接节点。将以下三个文件放到 `conf/` 目录：

```
backend/conf/
├── ca.crt      ← 链 CA 证书
├── sdk.crt     ← SDK 证书
└── sdk.key     ← SDK 私钥
```

证书通常由 FISCO BCOS 运维人员提供，或从链节点的 `sdk/` 目录获取。

```bash
# 检查证书是否存在
ls -la /data/app/wedpr-admin/backend/conf/*.crt
ls -la /data/app/wedpr-admin/backend/conf/*.key
```

### 9.4 修改 `application.properties`

```bash
vi /data/app/wedpr-admin/backend/conf/application.properties
```

主要确认以下配置：

```properties
# 后端监听端口（默认 6850，一般不用改）
server.port=6850

# 证书工具路径（改成服务器上的实际部署路径）
wedpr.cert.certScriptDir=/data/app/wedpr-admin/backend/conf
wedpr.cert.certScript=/data/app/wedpr-admin/backend/conf/cert_script.sh
wedpr.cert.rootCertPath=/data/app/wedpr-admin/backend/conf/cert/root
wedpr.cert.agencyCertPath=/data/app/wedpr-admin/backend/conf/cert/agency
```

---

## 10. 第七步：启动管理端后端

### 10.1 启动

```bash
cd /data/app/wedpr-admin/backend

# 确认 JAVA_HOME 已设置
echo $JAVA_HOME
java -version

# 启动服务
./start.sh
```

**启动成功的标志：**

```bash
# 查看启动日志
tail -f start.out
# 应看到类似：WedprAdminApplication start successfully!

# 或检查进程
ps aux | grep WedprAdminApplication

# 检查端口
ss -tlnp | grep 6850
# 或：netstat -tlnp | grep 6850
```

### 10.2 停止

```bash
cd /data/app/wedpr-admin/backend
./stop.sh
```

### 10.3 查看运行日志

```bash
# 启动输出
cat /data/app/wedpr-admin/backend/start.out

# 运行日志
tail -f /data/app/wedpr-admin/backend/logs/wedpr-admin/wedpr-admin.log
```

### 10.4 设置开机自启（可选）

创建 systemd 服务：

```bash
sudo tee /etc/systemd/system/wedpr-admin.service << 'EOF'
[Unit]
Description=WeDPR Admin Backend
After=network.target mysql.service

[Service]
Type=forking
User=root
WorkingDirectory=/data/app/wedpr-admin/backend
Environment=JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ExecStart=/data/app/wedpr-admin/backend/start.sh
ExecStop=/data/app/wedpr-admin/backend/stop.sh
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 启用并启动
sudo systemctl daemon-reload
sudo systemctl enable wedpr-admin
sudo systemctl start wedpr-admin
sudo systemctl status wedpr-admin
```

---

## 11. 第八步：部署管理端前端（Nginx）

生产环境中，前端静态文件由 Nginx 托管，API 请求反向代理到后端 6850 端口。

### 11.1 创建 Nginx 配置

```bash
sudo tee /etc/nginx/sites-available/wedpr-admin << 'EOF'
server {
    listen 80;
    server_name admin.example.com;    # 改成你的域名或服务器 IP

    # 前端静态文件目录
    root /data/app/wedpr-admin/frontend;
    index index.html;

    # 前端路由（Vue SPA 需要）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理到后端
    location /api/ {
        proxy_pass http://127.0.0.1:6850;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 超时设置（部分接口可能较慢）
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 7d;
        add_header Cache-Control "public, immutable";
    }
}
EOF
```

### 11.2 启用配置

```bash
# 创建软链接启用站点
sudo ln -sf /etc/nginx/sites-available/wedpr-admin /etc/nginx/sites-enabled/

# 删除默认站点（可选）
sudo rm -f /etc/nginx/sites-enabled/default

# 测试配置语法
sudo nginx -t
# 应显示：syntax is ok / test is successful

# 重新加载 Nginx
sudo systemctl reload nginx
```

### 11.3 配置 HTTPS（生产环境推荐）

如果有域名，建议用 Let's Encrypt 免费证书：

```bash
# 安装 certbot
sudo apt install -y certbot python3-certbot-nginx

# 自动配置 HTTPS（把域名换成你的）
sudo certbot --nginx -d admin.example.com
```

---

## 12. 第九步：创建管理员账号并登录

管理端登录需要 **`agency_admin`** 角色的账号（与站点端的 `admin_user` 不同）。

### 12.1 创建平台管理员账号

`wedpr_dml.sql` 默认只创建了站点端的 `admin` 用户（角色为 `admin_user`），管理端需要额外创建 `agency_admin` 角色用户：

```bash
mysql -u wedpr_admin -p wedpr3
```

```sql
-- 创建平台管理员账号（用户名：platform_admin，密码：Admin@123）
-- 密码使用 bcrypt 加密存储
INSERT INTO wedpr_user (username, password, status)
VALUES ('platform_admin', '{bcrypt}$2a$10$9ZhDOBp.sRKat4l14ygu/.LscxrMUcDAfeVOEPiYwbcRkoB09gCmi', 0);

-- 分配 agency_admin 角色（role_id = 10）
INSERT INTO wedpr_user_role (username, role_id) VALUES ('platform_admin', '10');

-- 验证
SELECT u.username, r.role_id
FROM wedpr_user u
JOIN wedpr_user_role r ON u.username = r.username
WHERE u.username = 'platform_admin';

EXIT;
```

> 上述密码哈希对应的明文密码为 **`Admin@123`**（WeDPR 默认测试密码）。
> 生产环境请登录后立即修改密码，或自行生成 bcrypt 哈希替换。

### 12.2 登录管理台

1. 浏览器打开 `http://你的服务器IP` 或 `http://admin.example.com`
2. 进入管理台登录页
3. 输入：
   - 账号：`platform_admin`
   - 密码：`Admin@123`
   - 验证码：按页面显示输入
4. 登录成功后进入管理台首页

### 12.3 登记机构（接入站点端）

登录后，在「机构管理」页面登记各站点端机构：

| 字段 | 说明 | 示例 |
|------|------|------|
| 机构英文名 | 必须与站点端 `wedpr.agency` 一致 | `agency0` |
| Gateway 地址 | 该机构的 C++ Gateway 地址 | `192.168.1.100:40600` |

---

## 13. 第十步：验证部署是否成功

按以下清单逐项检查：

### 13.1 后端健康检查

```bash
# 1. 进程在运行
ps aux | grep WedprAdminApplication

# 2. 端口在监听
ss -tlnp | grep 6850

# 3. API 可访问（获取验证码接口，无需登录）
curl -s http://127.0.0.1:6850/api/wedpr/v3/image-code | head -c 200
# 应返回 JSON，code 为 0
```

### 13.2 前端健康检查

```bash
# Nginx 能返回首页
curl -s -o /dev/null -w "%{http_code}" http://localhost/
# 应返回 200

# API 代理正常
curl -s http://localhost/api/wedpr/v3/image-code | head -c 200
# 应返回 JSON
```

### 13.3 功能验证

| 检查项 | 操作 | 预期结果 |
|--------|------|---------|
| 登录 | 用 platform_admin 登录 | 进入管理台 |
| 机构管理 | 登记一个机构 | 保存成功 |
| 区块链连通 | 查看日志无链连接报错 | `wedpr-admin.log` 无 ERROR |
| 站点数据 | 站点端跑任务后查看大屏 | 能看到统计数据 |

### 13.4 接入站点端后的完整验证

当站点端也部署完成后，确认以下条件全部满足：

```
✓ wedpr_agency 表中存在该机构且已启用
✓ gateway_endpoint 与实际 Gateway 地址一致
✓ 管理端 gateway_targets 能连通该 Gateway
✓ 站点端与管理端合约地址、chain.group_id 一致
✓ 站点端 ReportQuartzJob 正常运行
✓ 站点端 ResourceSyncer 正常运行
```

---

## 14. 日常运维命令

```bash
# 启动后端
cd /data/app/wedpr-admin/backend && ./start.sh

# 停止后端
cd /data/app/wedpr-admin/backend && ./stop.sh

# 查看后端日志
tail -f /data/app/wedpr-admin/backend/logs/wedpr-admin/wedpr-admin.log

# 重启 Nginx
sudo systemctl reload nginx

# 查看 Nginx 日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# 检查 MySQL 连接
mysql -u wedpr_admin -p wedpr3 -e "SELECT COUNT(*) FROM wedpr_agency;"
```

---

## 15. 常见问题

### Q1：构建时报错 `project :wedpr-common-utils not found`

**原因：** `WeDPR-admin` 与 `WeDPR` 目录不是同级关系。

**解决：**

```bash
# 确认目录结构
ls ../WeDPR/wedpr-common/utils
# 如果找不到，需要将 WeDPR 仓库放到 WeDPR-admin 的同级目录
```

### Q2：启动后端报数据库连接失败

**排查步骤：**

```bash
# 1. 确认 MySQL 在运行
sudo systemctl status mysql

# 2. 手动测试连接
mysql -u wedpr_admin -p -h 127.0.0.1 wedpr3

# 3. 检查 wedpr.properties 中的 url、username、password 是否正确
cat /data/app/wedpr-admin/backend/conf/wedpr.properties | grep mybatis
```

### Q3：登录提示「无权限访问该接口」

**原因：** 登录账号的角色不是 `agency_admin`。

**解决：** 按 [第 12.1 步](#121-创建平台管理员账号) 创建 `agency_admin` 角色用户。

### Q4：管理端看不到站点端数据

**排查清单：**

1. 机构是否已在「机构管理」中登记？
2. `wedpr.transport.gateway_targets` 地址是否正确且网络可达？
   ```bash
   telnet 192.168.1.100 45600
   ```
3. 合约地址是否与站点端一致？
4. 站点端 `ReportQuartzJob` 是否在运行？（查看站点端日志）

### Q5：前端页面空白或 404

**排查步骤：**

```bash
# 1. 确认前端文件存在
ls /data/app/wedpr-admin/frontend/index.html

# 2. 确认 Nginx 配置中 root 路径正确
sudo nginx -t

# 3. 查看 Nginx 错误日志
sudo tail -20 /var/log/nginx/error.log
```

### Q6：`start.sh` 提示 JAVA_HOME 未配置

```bash
# 查找 Java 安装路径
update-alternatives --list java
# 或
ls /usr/lib/jvm/

# 设置 JAVA_HOME 后重新启动
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
cd /data/app/wedpr-admin/backend
./start.sh
```

### Q7：端口 6850 被占用

```bash
# 查看占用进程
ss -tlnp | grep 6850

# 停止旧进程
cd /data/app/wedpr-admin/backend
./stop.sh
```

---

## 16. 附录

### 16.1 完整端口列表

| 端口 | 服务 | 说明 |
|------|------|------|
| 80/443 | Nginx | 前端页面 + API 代理 |
| 6850 | wedpr-admin 后端 | 仅本机或内网 |
| 3306 | MySQL | 数据库 |
| 6001 | Transport | 管理端消息监听 |
| 30202+ | FISCO BCOS | 区块链节点 RPC |

### 16.2 配置文件速查

| 文件 | 关键配置项 |
|------|-----------|
| `wedpr.properties` | 数据库、合约地址、Gateway 地址 |
| `config.toml` | 区块链节点 peers |
| `application.properties` | 服务端口 6850、证书路径 |
| `conf/ca.crt` + `sdk.crt` + `sdk.key` | 区块链 SSL 证书 |

### 16.3 角色说明

| 角色 | 使用场景 | 登录入口 |
|------|---------|---------|
| `agency_admin` | 平台管理端 | `wedpr-web-admin`（管理台） |
| `admin_user` | 机构站点端 | `wedpr-web`（站点台） |

### 16.4 相关文档

- 管理端与站点端接入规范：`docs/architecture/phase1_admin_site_integration.md`
- 区块链部署与同步：`docs/architecture/phase5_blockchain_contract_deploy_and_onchain_data_sync.md`
- 站点端部署：参见 `WeDPR/` 仓库相关文档

### 16.5 一键打包脚本参考

可将以下内容保存为 `WeDPR-admin/scripts/pack-deploy.sh`：

```bash
#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ADMIN_ROOT="$(dirname "$SCRIPT_DIR")"
WEDPR_ROOT="$(dirname "$ADMIN_ROOT")/WeDPR"
OUTPUT="/tmp/wedpr-admin-deploy"

echo "=== WeDPR 管理端部署包构建 ==="

# 检查依赖
if [ ! -d "$WEDPR_ROOT/wedpr-common" ]; then
    echo "错误：找不到 $WEDPR_ROOT/wedpr-common"
    echo "请确保 WeDPR 与 WeDPR-admin 在同一父目录下"
    exit 1
fi

# 构建后端
echo ">>> 构建后端..."
cd "$ADMIN_ROOT"
./gradlew :wedpr-admin:jar -x test

# 构建前端
echo ">>> 构建前端..."
cd "$ADMIN_ROOT/wedpr-web-admin"
npm install
npm run build:pro

# 打包
echo ">>> 打包..."
rm -rf "$OUTPUT"
mkdir -p "$OUTPUT"
cp -r "$ADMIN_ROOT/wedpr-admin/dist" "$OUTPUT/backend"
cp -r "$ADMIN_ROOT/wedpr-web-admin/manage" "$OUTPUT/frontend"
cp "$WEDPR_ROOT/wedpr-builder/db/wedpr_ddl.sql" "$OUTPUT/"
cp "$WEDPR_ROOT/wedpr-builder/db/wedpr_dml.sql" "$OUTPUT/"

cd /tmp
tar czvf wedpr-admin-deploy.tar.gz wedpr-admin-deploy/

echo ""
echo "=== 打包完成 ==="
echo "文件位置：/tmp/wedpr-admin-deploy.tar.gz"
echo "上传到服务器后解压到 /data/app/wedpr-admin 即可部署"
ls -lh /tmp/wedpr-admin-deploy.tar.gz
```

使用方法：

```bash
chmod +x WeDPR-admin/scripts/pack-deploy.sh
./WeDPR-admin/scripts/pack-deploy.sh
```

---

*文档版本：2026-06-08 | 适用 WeDPR 3.1.0*
