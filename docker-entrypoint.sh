#!/bin/sh
set -e

# 当 /app/resources 被挂载为空数据卷时, 用镜像内置资源初始化它。
# 这样既能持久化 tale.db 与上传文件, 又不会让空卷覆盖模板/静态资源/schema.sql。
if [ -d /app/resources ] && [ -z "$(ls -A /app/resources 2>/dev/null)" ]; then
    echo "[entrypoint] resources volume is empty, seeding from defaults..."
    cp -a /app/resources_default/. /app/resources/
fi

echo "[entrypoint] starting tale: java ${JAVA_OPTS} -jar tale-latest.jar $*"
exec java ${JAVA_OPTS} -jar /app/tale-latest.jar "$@"
