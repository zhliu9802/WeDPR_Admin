# WeDPR-admin

WeDPR 平台管理端，与站点端（`WeDPR/`）物理解耦，独立部署。

## 目录结构

| 目录 | 说明 | 默认端口 |
|------|------|---------|
| `wedpr-admin/` | 管理端后端（Spring Boot） | 6850 |
| `wedpr-web-admin/` | 管理端前端（Vue 2） | 3001（开发） |

## 与站点端的关系

- **不直接 HTTP 互调**：管理端通过 Gateway Transport 与区块链同步汇聚各机构站点数据
- **共享组件**：后端依赖 `../WeDPR/wedpr-common` 与 `../WeDPR/wedpr-components` 公共模块
- **独立数据库**：管理端使用独立 MySQL 实例，表结构见 `../WeDPR/wedpr-builder/db/wedpr_ddl.sql`

## 后端构建与启动

```bash
cd WeDPR-admin
./gradlew :wedpr-admin:jar

cd wedpr-admin/dist
./start.sh
```

## 前端开发

```bash
cd wedpr-web-admin
npm install
npm run serve
```

开发环境 API 代理到 `http://127.0.0.1:6850`。

## 前置依赖

1. 先构建站点端共享组件（首次或组件变更后）：

```bash
cd ../WeDPR
./gradlew build -x test
```

2. 部署 MySQL 并执行 DDL
3. 配置 `wedpr-admin/conf/wedpr.properties` 中的区块链与 Transport 参数

## 部署文档

**完整部署指南（从零开始，含所有安装命令）：** [docs/deployment_guide.md](docs/deployment_guide.md)

一键打包脚本：

```bash
chmod +x scripts/pack-deploy.sh
./scripts/pack-deploy.sh
```

详细接入规范见项目根目录 `docs/architecture/phase1_admin_site_integration.md`。
