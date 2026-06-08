# WeDPR 管理端

WeDPR（WeBank Data Privacy Reserve）隐私计算协作平台的**管理端**项目。面向多机构数据协作场景，提供机构治理、数据资源管理、隐私计算任务汇聚与链上审计能力。

管理端与各机构**站点端物理解耦、独立部署**，通过 WeDPR Gateway 消息总线与 FISCO BCOS 区块链同步汇聚各机构上报的项目、任务与数据集信息，实现统一管理与可审计。

## 项目结构

```
WeDPR管理端/
├── WeDPR-admin/          # 管理端（后端 + 前端）
│   ├── wedpr-admin/      # 后端服务，Spring Boot，默认端口 6850
│   └── wedpr-web-admin/  # 前端管理界面，Vue 2 + Element UI，开发端口 3001
├── WeDPR/                # 站点端共享组件（管理端编译依赖）
│   ├── wedpr-builder/    # 构建与部署脚本、数据库 DDL
│   ├── wedpr-common/     # 公共库：utils 工具、protocol 协议
│   └── wedpr-components/ # 30+ 功能组件（transport、sync、dataset、crypto 等）
└── docs/                 # 部署文档
```

管理端后端通过相对路径引用 `WeDPR/` 下的公共组件，因此两者需保持在同一父目录中。

## 技术栈

**后端**

| 类别 | 技术 |
|------|------|
| 语言 / 构建 | JDK 1.8、Gradle |
| 框架 | Spring Boot 2.5、Spring Security、MyBatis Plus 3.5、Druid |
| 数据库 | MySQL 8 |
| 区块链 | FISCO BCOS SDK 3.7、WeDPR Gateway SDK 3.0 |
| 密码学 | BouncyCastle、国密支持 |
| 其他 | Log4j2、JWT、Quartz、Swagger（Springfox） |

**前端**

| 类别 | 技术 |
|------|------|
| 框架 | Vue 2.6、Vue Router 3、Vuex 3 |
| UI | Element UI 2.15、webank-wedpr-ui |
| 构建 | Vue CLI 5 |
| 其他 | axios、echarts、dayjs、sm-crypto（国密） |

## 核心功能

- **机构管理**：多机构（Agency）注册、信息维护与网关通信配置
- **证书管理**：机构证书的生成与管理
- **数据资源**：数据集元数据汇聚、权限管理、差分隐私配置
- **项目空间**：跨机构协作项目的查看与管理
- **任务管理**：隐私计算任务（PIR、LR、XGB 等）状态汇聚与 DAG 工作流追踪
- **日志审计**：基于区块链存证的资源同步操作审计
- **数据总览**：多机构数据资产的可视化大屏

## 架构说明

管理端不直接 HTTP 调用站点端，而是采用**消息驱动 + 区块链审计**的汇聚架构：

1. 各机构站点端通过 WeDPR Gateway 上报 `PROJECT_REPORT`、`JOB_REPORT`、`JOB_DATASET_REPORT` 等消息
2. 管理端 Transport 层订阅相应 Topic，异步反序列化并落库到本地 MySQL
3. 资源同步操作经 FISCO BCOS 合约记录，保证可追溯、可审计
4. 管理端拥有独立的 MySQL 实例，与站点端数据库相互隔离

## 快速开始

### 前置依赖

- JDK 1.8
- MySQL 8（独立实例，执行 `WeDPR/wedpr-builder/db/` 下的 DDL）
- 可连通的 FISCO BCOS 节点与各机构 Gateway
- Node.js（前端开发，建议 16+）

### 构建共享组件（首次或组件变更后）

```bash
cd WeDPR
./gradlew build -x test
```

### 后端构建与启动

```bash
cd WeDPR-admin
./gradlew :wedpr-admin:jar

cd wedpr-admin/dist
./start.sh
```

后端默认监听 `6850`，关键配置见 `wedpr-admin/conf/`（数据库连接、区块链群组、Transport 节点与网关地址、JWT 密钥等）。

### 前端开发

```bash
cd WeDPR-admin/wedpr-web-admin
npm install
npm run serve
```

开发环境访问 `http://localhost:3001`，API 默认代理到 `http://127.0.0.1:6850`。生产构建：

```bash
npm run build:pro   # 产物输出到 manage/，由 Nginx 托管
```

## 一键部署

根目录提供 `deploy.sh` 脚本，封装了从构建到启动的完整流程。

```bash
# 1. 复制并修改配置（含数据库、区块链、网关等参数）
cp deploy.conf.example deploy.conf
vim deploy.conf

# 2. 一键部署：构建 -> 建库导表 -> 渲染配置 -> 安装 -> Nginx -> 启动
./deploy.sh all
```

也可分步执行：

| 命令 | 说明 |
|------|------|
| `./deploy.sh build` | 构建共享组件、后端 jar 与前端静态文件 |
| `./deploy.sh init-db` | 创建数据库并导入 DDL/DML |
| `./deploy.sh config` | 按 `deploy.conf` 渲染后端配置文件 |
| `./deploy.sh install` | 安装产物到 `DEPLOY_DIR` / `WEB_DIR` |
| `sudo ./deploy.sh nginx` | 生成并启用 Nginx 反向代理 |
| `./deploy.sh gen-secret [--write]` | 生成随机 JWT 密钥，`--write` 直接写回 deploy.conf |
| `./deploy.sh start` / `stop` / `restart` / `status` | 管理后端进程 |

各配置项的含义见 [`deploy.conf.example`](deploy.conf.example) 内的逐项注释。`deploy.conf` 含数据库密码与 JWT 密钥等敏感信息，已被 `.gitignore` 忽略，请勿提交。完整的从零部署说明见 [`docs/`](docs/) 目录。

## 部署文档

完整的从零部署指南（含环境准备、MySQL 安装、配置说明、运维命令与常见问题）见 [`docs/`](docs/) 目录，最新版本为 [`deployment_guide_v1.2.md`](docs/deployment_guide_v1.2.md)。

## 安全提示

- `wedpr-admin/conf/` 下的配置文件包含数据库凭证、JWT 密钥等敏感信息，生产环境请妥善管理，避免提交真实凭证。
- 仓库中的 `sdk.key` 等密钥材料仅供示例 / 开发使用，生产部署请替换为独立生成的密钥。
