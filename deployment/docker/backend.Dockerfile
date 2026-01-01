# 后端 Dockerfile
FROM maven:3.8.6-openjdk-8 AS builder

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY backend/pom.xml .
RUN mvn dependency:go-offline

# 复制源代码并构建
COPY backend/src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:8-jdk-alpine

WORKDIR /app

# 复制构建产物
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
