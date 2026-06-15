# ============================================
# Multi-stage Dockerfile for Tale Blog
# Blade MVC + Netty + SQLite
# ============================================

# ---------- Build Stage ----------
FROM eclipse-temurin:8-jdk AS builder

# Install Maven
RUN apt-get update && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /build

# Cache dependencies first (layer optimization)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src/ src/
COPY package.xml .
RUN mvn clean package -P prod -DskipTests -B \
    && mkdir -p /build/target/dist/tale \
    && tar -xzf /build/target/dist/tale.tar.gz -C /build/target/dist/tale

# ---------- Runtime Stage ----------
FROM eclipse-temurin:8-jre

LABEL maintainer="tale-blog"
LABEL description="Tale Blog - Lightweight Blade MVC Blog System"

# Create non-root user for security
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r tale && useradd -r -g tale tale

WORKDIR /app

# Copy build artifacts from builder stage
COPY --from=builder /build/target/dist/tale/ ./

# Create directories for persistent data
RUN mkdir -p /app/data /app/logs /app/upload /app/plugins /app/templates/themes \
    && chown -R tale:tale /app

# Switch to non-root user
USER tale

# Blade MVC default port
EXPOSE 9000

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:9000/ || exit 1

# JVM options via environment variable for flexibility
ENV JAVA_OPTS="-Xms256m -Xmx256m"

# Entry point
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar tale-latest.jar --app.env=prod"]
