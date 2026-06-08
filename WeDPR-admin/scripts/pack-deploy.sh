#!/bin/bash
# WeDPR 管理端部署包一键打包脚本
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ADMIN_ROOT="$(dirname "$SCRIPT_DIR")"
WEDPR_ROOT="$(dirname "$ADMIN_ROOT")/WeDPR"
OUTPUT="/tmp/wedpr-admin-deploy"

echo "=== WeDPR 管理端部署包构建 ==="

# 检查 WeDPR 共享组件目录是否存在
if [ ! -d "$WEDPR_ROOT/wedpr-common" ]; then
    echo "错误：找不到 $WEDPR_ROOT/wedpr-common"
    echo "请确保 WeDPR 与 WeDPR-admin 在同一父目录下"
    exit 1
fi

# 构建后端
echo ">>> 构建后端..."
cd "$ADMIN_ROOT"
chmod +x gradlew
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
