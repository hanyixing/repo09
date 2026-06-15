# syntax=docker/dockerfile:1

# ============================================================
# Stage 1: 构建 tale 发布包 (target/dist/tale.tar.gz)
# ============================================================
FROM maven:3.6.3-jdk-8 AS builder

WORKDIR /build

# 复制 POM 与装配描述符及源码
COPY pom.xml package.xml ./
COPY src ./src
COPY bin ./bin

# 以 prod profile 打包(maven-assembly 生成 dist/tale.tar.gz); 镜像构建阶段跳过测试, 由 CI 单独执行
RUN mvn -B -Pprod -DskipTests clean package

# ============================================================
# Stage 2: 运行时镜像
# ============================================================
FROM eclipse-temurin:8-jre AS runtime

LABEL org.opencontainers.image.title="tale" \
      org.opencontainers.image.description="Tale blog (Blade MVC + SQLite)"

WORKDIR /app

# 解压发布包: 得到 tale-latest.jar / lib/ / resources/ / plugins/ / tool
COPY --from=builder /build/target/dist/tale.tar.gz /tmp/tale.tar.gz
RUN tar -xzf /tmp/tale.tar.gz -C /app \
    && rm -f /tmp/tale.tar.gz \
    # 备份内置资源, 供挂载卷首次启动时初始化(见 docker-entrypoint.sh)
    && cp -r /app/resources /app/resources_default

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

ENV JAVA_OPTS="-Xms256m -Xmx256m -Dfile.encoding=UTF-8"

# Blade 默认监听端口
EXPOSE 9000

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["--app.env=prod"]
