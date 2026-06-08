#!/bin/bash
# =============================================================================
# WeDPR 管理端一键部署脚本
# =============================================================================
# 用法:
#   ./deploy.sh <command>
#
# 命令:
#   build          本地构建后端 jar + 前端静态文件
#   init-db        创建数据库并导入表结构（DDL/DML）
#   config         按 deploy.conf 渲染 conf/ 下的配置文件
#   install        将构建产物安装到 DEPLOY_DIR / WEB_DIR
#   nginx          生成并启用 Nginx 反向代理配置（需 root）
#   start          启动管理端后端
#   stop           停止管理端后端
#   restart        重启管理端后端
#   status         查看运行状态与访问地址
#   setup-timezone 设置系统时区（需 root）
#   all            一键执行: build -> init-db -> config -> install -> [nginx] -> start
#
# 配置: 首次使用先 cp deploy.conf.example deploy.conf 并按需修改
# =============================================================================
set -euo pipefail

# ----------------------------- 路径与常量 ------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR"
ADMIN_ROOT="$ROOT_DIR/WeDPR-admin"
WEDPR_ROOT="$ROOT_DIR/WeDPR"
BACKEND_SRC="$ADMIN_ROOT/wedpr-admin"
FRONTEND_SRC="$ADMIN_ROOT/wedpr-web-admin"
DDL_FILE="$WEDPR_ROOT/wedpr-builder/db/wedpr_ddl.sql"
DML_FILE="$WEDPR_ROOT/wedpr-builder/db/wedpr_dml.sql"
CONF_FILE="$ROOT_DIR/deploy.conf"

# ----------------------------- 日志辅助 --------------------------------------
log_info()  { echo -e "\033[32m[INFO]\033[0m  $*"; }
log_warn()  { echo -e "\033[33m[WARN]\033[0m  $*"; }
log_error() { echo -e "\033[31m[ERROR]\033[0m $*" >&2; }
die()       { log_error "$*"; exit 1; }

# ----------------------------- 加载配置 --------------------------------------
load_conf() {
    [ -f "$CONF_FILE" ] || die "未找到 deploy.conf，请先执行: cp deploy.conf.example deploy.conf 并修改"
    # shellcheck disable=SC1090
    source "$CONF_FILE"

    # 默认值兜底
    : "${ADMIN_IP:?deploy.conf 缺少 ADMIN_IP}"
    : "${ADMIN_AGENCY:=ADMIN}"
    : "${HTTP_PORT:=6850}"
    : "${MYSQL_HOST:=127.0.0.1}"
    : "${MYSQL_PORT:=3306}"
    : "${MYSQL_DB:=wedpr_admin}"
    : "${MYSQL_USER:=wedpr}"
    : "${MYSQL_PASSWORD:?deploy.conf 缺少 MYSQL_PASSWORD}"
    : "${MYSQL_ROOT_USER:=root}"
    : "${MYSQL_ROOT_PASSWORD:=}"
    : "${GATEWAY_TARGETS:=ipv4:127.0.0.1:45600,127.0.0.1:45601}"
    : "${TRANSPORT_NODE_ID:=admin}"
    : "${TRANSPORT_LISTEN_PORT:=6001}"
    : "${BLOCKCHAIN_GROUP:=group0}"
    : "${BLOCKCHAIN_PEERS:=127.0.0.1:30202,127.0.0.1:30203}"
    : "${RECORDER_FACTORY_CONTRACT:?deploy.conf 缺少 RECORDER_FACTORY_CONTRACT}"
    : "${SEQUENCER_CONTRACT:?deploy.conf 缺少 SEQUENCER_CONTRACT}"
    : "${JWT_SECRET:=please-change-me}"
    : "${DEPLOY_DIR:=/data/app/wedpr-admin}"
    : "${WEB_DIR:=/data/app/wedpr-admin/web}"
    : "${ENABLE_NGINX:=true}"
    : "${NGINX_PORT:=80}"
    : "${SKIP_TESTS:=true}"
    : "${SKIP_NPM_INSTALL:=false}"
    : "${TIMEZONE:=Asia/Shanghai}"

    [ "$JWT_SECRET" = "please-change-me" ] && \
        log_warn "JWT_SECRET 仍为示例值，生产环境请在 deploy.conf 中修改为随机强密钥"
}

# 将 peers=IP:PORT,IP:PORT 转为 config.toml 的数组字面量: "IP:PORT", "IP:PORT"
peers_to_toml() {
    local out="" p
    IFS=',' read -ra _peers <<< "$BLOCKCHAIN_PEERS"
    for p in "${_peers[@]}"; do
        p="$(echo "$p" | xargs)"   # trim
        [ -z "$p" ] && continue
        [ -n "$out" ] && out="$out, "
        out="$out\"$p\""
    done
    echo "$out"
}

require_mysql_client() {
    command -v mysql >/dev/null 2>&1 || die "未找到 mysql 客户端，请先安装 mysql-client"
}

# mysql 调用封装：root 密码非空走 root，否则用业务账号
mysql_admin() {
    if [ -n "$MYSQL_ROOT_PASSWORD" ]; then
        mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_ROOT_USER" -p"$MYSQL_ROOT_PASSWORD" "$@"
    else
        mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$@"
    fi
}

mysql_app() {
    mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$@"
}

# ----------------------------- build ----------------------------------------
cmd_build() {
    log_info "开始构建 WeDPR 管理端"
    [ -d "$WEDPR_ROOT/wedpr-common" ] || \
        die "找不到 $WEDPR_ROOT/wedpr-common，请确保 WeDPR 与 WeDPR-admin 在同一父目录下"

    log_info ">>> 构建共享组件 (WeDPR)"
    cd "$WEDPR_ROOT"
    chmod +x gradlew 2>/dev/null || true
    if [ "$SKIP_TESTS" = "true" ]; then
        ./gradlew build -x test
    else
        ./gradlew build
    fi

    log_info ">>> 构建后端 jar (wedpr-admin)"
    cd "$ADMIN_ROOT"
    chmod +x gradlew 2>/dev/null || true
    if [ "$SKIP_TESTS" = "true" ]; then
        ./gradlew :wedpr-admin:jar -x test
    else
        ./gradlew :wedpr-admin:jar
    fi
    [ -d "$BACKEND_SRC/dist" ] || die "后端构建产物 $BACKEND_SRC/dist 不存在，构建可能失败"

    log_info ">>> 构建前端 (wedpr-web-admin)"
    cd "$FRONTEND_SRC"
    command -v npm >/dev/null 2>&1 || die "未找到 npm，请先安装 Node.js"
    if [ "$SKIP_NPM_INSTALL" != "true" ]; then
        npm install
    fi
    npm run build:pro
    [ -d "$FRONTEND_SRC/manage" ] || die "前端构建产物 $FRONTEND_SRC/manage 不存在，构建可能失败"

    log_info "构建完成: 后端 $BACKEND_SRC/dist，前端 $FRONTEND_SRC/manage"
}

# ----------------------------- init-db --------------------------------------
cmd_init_db() {
    require_mysql_client
    [ -f "$DDL_FILE" ] || die "找不到 DDL 文件: $DDL_FILE"
    log_info "初始化数据库 $MYSQL_DB @ $MYSQL_HOST:$MYSQL_PORT"

    if [ -n "$MYSQL_ROOT_PASSWORD" ]; then
        log_info ">>> 使用 root 创建数据库与业务账号"
        mysql_admin <<SQL
CREATE DATABASE IF NOT EXISTS \`$MYSQL_DB\` DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS '$MYSQL_USER'@'%' IDENTIFIED BY '$MYSQL_PASSWORD';
GRANT ALL PRIVILEGES ON \`$MYSQL_DB\`.* TO '$MYSQL_USER'@'%';
FLUSH PRIVILEGES;
SQL
    else
        log_info ">>> ROOT 密码为空，尝试用业务账号直接建库"
        mysql_app -e "CREATE DATABASE IF NOT EXISTS \`$MYSQL_DB\` DEFAULT CHARACTER SET utf8mb4;"
    fi

    log_info ">>> 导入表结构 (DDL)"
    mysql_app "$MYSQL_DB" < "$DDL_FILE"
    if [ -f "$DML_FILE" ]; then
        log_info ">>> 导入初始化数据 (DML)"
        mysql_app "$MYSQL_DB" < "$DML_FILE"
    fi
    log_info "数据库初始化完成"
}

# ----------------------------- config ---------------------------------------
# 渲染后端构建产物 dist/conf 下的配置文件
cmd_config() {
    local conf_dir="$BACKEND_SRC/dist/conf"
    [ -d "$conf_dir" ] || die "未找到 $conf_dir，请先执行 ./deploy.sh build"
    log_info "渲染配置文件: $conf_dir"

    local app_props="$conf_dir/application.properties"
    local wedpr_props="$conf_dir/wedpr.properties"
    local toml="$conf_dir/config.toml"
    local jdbc="jdbc:mysql://$MYSQL_HOST:$MYSQL_PORT/$MYSQL_DB?characterEncoding=UTF-8&allowMultiQueries=true"

    # application.properties: 端口 + 证书路径（默认指向 DEPLOY_DIR/conf）
    if [ -f "$app_props" ]; then
        set_prop "$app_props" "server.port" "$HTTP_PORT"
        set_prop "$app_props" "wedpr.cert.certScriptDir" "$DEPLOY_DIR/conf"
        set_prop "$app_props" "wedpr.cert.certScript" "$DEPLOY_DIR/conf/cert_script.sh"
        set_prop "$app_props" "wedpr.cert.rootCertPath" "$DEPLOY_DIR/conf/cert/root"
        set_prop "$app_props" "wedpr.cert.agencyCertPath" "$DEPLOY_DIR/conf/cert/agency"
    fi

    # wedpr.properties: 数据库、链、transport、合约、JWT
    [ -f "$wedpr_props" ] || die "未找到 $wedpr_props"
    set_prop "$wedpr_props" "wedpr.mybatis.url" "$jdbc"
    set_prop "$wedpr_props" "wedpr.mybatis.username" "$MYSQL_USER"
    set_prop "$wedpr_props" "wedpr.mybatis.password" "$MYSQL_PASSWORD"
    set_prop "$wedpr_props" "wedpr.admin_agency" "$ADMIN_AGENCY"
    set_prop "$wedpr_props" "wedpr.chain.group_id" "$BLOCKCHAIN_GROUP"
    set_prop "$wedpr_props" "wedpr.sync.recorder.factory.contract_address" "$RECORDER_FACTORY_CONTRACT"
    set_prop "$wedpr_props" "wedpr.sync.sequencer.contract_address" "$SEQUENCER_CONTRACT"
    set_prop "$wedpr_props" "wedpr.transport.nodeID" "$TRANSPORT_NODE_ID"
    set_prop "$wedpr_props" "wedpr.transport.gateway_targets" "$GATEWAY_TARGETS"
    set_prop "$wedpr_props" "wedpr.transport.host_ip" "$ADMIN_IP"
    set_prop "$wedpr_props" "wedpr.transport.listen_port" "$TRANSPORT_LISTEN_PORT"
    set_prop "$wedpr_props" "wedpr.user.jwt.secret" "$JWT_SECRET"

    # config.toml: 群组与节点 peers
    if [ -f "$toml" ]; then
        local peers; peers="$(peers_to_toml)"
        # defaultGroup="xxx"
        sed -i.bak -E "s|^(defaultGroup[[:space:]]*=).*|\1\"$BLOCKCHAIN_GROUP\"|" "$toml"
        # peers=[...]（可能含行尾注释，替换到行末）
        sed -i.bak -E "s|^(peers[[:space:]]*=).*|\1[$peers]|" "$toml"
        rm -f "$toml.bak"
    fi

    log_info "配置渲染完成"
    log_warn "区块链通信证书 (ca.crt/sdk.crt/sdk.key) 请确认 $conf_dir 下为目标环境的真实证书"
}

# set_prop <file> <key> <value>：替换 properties 中的 key=value，存在则改，不存在则追加
# 使用 awk 处理，避免 value 含特殊字符时 sed 转义问题
set_prop() {
    local file="$1" key="$2" value="$3" tmp
    tmp="$(mktemp)"
    awk -v k="$key" -v v="$value" '
        BEGIN { done=0 }
        {
            # 匹配 key= 或 key = 开头（忽略前导空白），跳过注释行
            line=$0
            sub(/^[ \t]+/, "", line)
            if (index(line, "#") == 1) { print $0; next }
            split(line, a, "=")
            gsub(/[ \t]+$/, "", a[1])
            if (a[1] == k) { print k "=" v; done=1 }
            else { print $0 }
        }
        END { if (!done) print k "=" v }
    ' "$file" > "$tmp"
    mv "$tmp" "$file"
}

# ----------------------------- install --------------------------------------
cmd_install() {
    [ -d "$BACKEND_SRC/dist" ] || die "未找到后端产物 $BACKEND_SRC/dist，请先 build"
    [ -d "$FRONTEND_SRC/manage" ] || die "未找到前端产物 $FRONTEND_SRC/manage，请先 build"

    log_info ">>> 安装后端到 $DEPLOY_DIR"
    mkdir -p "$DEPLOY_DIR"
    cp -r "$BACKEND_SRC/dist/." "$DEPLOY_DIR/"
    chmod +x "$DEPLOY_DIR/start.sh" "$DEPLOY_DIR/stop.sh" 2>/dev/null || true

    log_info ">>> 安装前端到 $WEB_DIR"
    mkdir -p "$WEB_DIR"
    cp -r "$FRONTEND_SRC/manage/." "$WEB_DIR/"

    log_info "安装完成"
}

# ----------------------------- nginx ----------------------------------------
cmd_nginx() {
    [ "$(id -u)" -eq 0 ] || die "配置 Nginx 需要 root 权限，请用 sudo 执行"
    command -v nginx >/dev/null 2>&1 || die "未找到 nginx，请先安装"

    local site_file="/etc/nginx/sites-available/wedpr-admin"
    log_info "生成 Nginx 配置: $site_file"
    cat > "$site_file" <<NGINX
server {
    listen $NGINX_PORT;
    server_name $ADMIN_IP;

    client_max_body_size 50M;

    location / {
        root $WEB_DIR;
        index index.html;
        try_files \$uri \$uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:$HTTP_PORT/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }
}
NGINX

    if [ -d /etc/nginx/sites-enabled ]; then
        ln -sf "$site_file" /etc/nginx/sites-enabled/wedpr-admin
    fi
    nginx -t && (systemctl reload nginx 2>/dev/null || nginx -s reload)
    log_info "Nginx 配置完成，访问: http://$ADMIN_IP:$NGINX_PORT/"
}

# ----------------------------- start/stop -----------------------------------
cmd_start() {
    [ -f "$DEPLOY_DIR/start.sh" ] || die "未找到 $DEPLOY_DIR/start.sh，请先 install"
    log_info "启动管理端后端"
    (cd "$DEPLOY_DIR" && bash start.sh)
    cmd_status
}

cmd_stop() {
    if [ -f "$DEPLOY_DIR/stop.sh" ]; then
        log_info "停止管理端后端"
        (cd "$DEPLOY_DIR" && bash stop.sh) || true
    else
        log_warn "未找到 $DEPLOY_DIR/stop.sh，跳过"
    fi
}

cmd_restart() { cmd_stop; sleep 2; cmd_start; }

cmd_status() {
    local pid
    pid="$(ps aux | grep 'com.webank.wedpr.WedprAdminApplication' | grep "$DEPLOY_DIR/" | awk '{print $2}' || true)"
    if [ -n "$pid" ]; then
        log_info "运行中 (PID: $pid)"
        log_info "后端 API : http://$ADMIN_IP:$HTTP_PORT"
        [ "$ENABLE_NGINX" = "true" ] && log_info "管理界面 : http://$ADMIN_IP:$NGINX_PORT/"
    else
        log_warn "管理端后端未运行"
    fi
}

# ----------------------------- setup-timezone -------------------------------
cmd_setup_timezone() {
    [ "$(id -u)" -eq 0 ] || die "设置时区需要 root 权限，请用 sudo 执行"
    timedatectl set-timezone "$TIMEZONE"
    log_info "系统时区已设置为 $TIMEZONE"
}

# ----------------------------- all ------------------------------------------
cmd_all() {
    cmd_build
    cmd_init_db
    cmd_config
    cmd_install
    if [ "$ENABLE_NGINX" = "true" ]; then
        if [ "$(id -u)" -eq 0 ]; then
            cmd_nginx
        else
            log_warn "ENABLE_NGINX=true 但当前非 root，跳过 Nginx。请稍后执行: sudo ./deploy.sh nginx"
        fi
    fi
    cmd_start
    log_info "全部完成"
}

# ----------------------------- main -----------------------------------------
usage() {
    sed -n '2,30p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
}

main() {
    local cmd="${1:-}"
    case "$cmd" in
        build)          load_conf; cmd_build ;;
        init-db)        load_conf; cmd_init_db ;;
        config)         load_conf; cmd_config ;;
        install)        load_conf; cmd_install ;;
        nginx)          load_conf; cmd_nginx ;;
        start)          load_conf; cmd_start ;;
        stop)           load_conf; cmd_stop ;;
        restart)        load_conf; cmd_restart ;;
        status)         load_conf; cmd_status ;;
        setup-timezone) load_conf; cmd_setup_timezone ;;
        all)            load_conf; cmd_all ;;
        ""|-h|--help|help) usage ;;
        *)              log_error "未知命令: $cmd"; echo; usage; exit 1 ;;
    esac
}

main "$@"
