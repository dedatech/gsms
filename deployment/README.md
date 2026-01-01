# GSMS 部署指南

本文档介绍如何使用 Docker Compose 部署 GSMS 系统。

## 环境要求

- **Docker** 20.10+
- **Docker Compose** 2.0+

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-username/gsms.git
cd gsms
```

### 2. 启动服务

```bash
# 启动所有服务（MySQL、后端、前端）
docker-compose up -d

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
```

### 3. 访问应用

- **前端应用**: http://localhost
- **后端 API**: http://localhost:8080
- **Swagger 文档**: http://localhost:8080/swagger-ui.html
- **健康检查**: http://localhost:8080/actuator/health

### 4. 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止服务并删除数据卷（谨慎使用）
docker-compose down -v
```

## 服务说明

### MySQL

- **端口**: 3306
- **用户**: gsms / gsms123
- **Root 密码**: root123
- **数据卷**: mysql-data（持久化存储）

### 后端服务

- **端口**: 8080
- **健康检查**: /actuator/health
- **依赖**: MySQL（等待 MySQL 就绪后启动）

### 前端服务

- **端口**: 80
- **依赖**: 后端服务

## 配置说明

### 环境变量

可以在 `docker-compose.yml` 中修改以下环境变量：

**MySQL**:
- `MYSQL_ROOT_PASSWORD`: Root 密码
- `MYSQL_DATABASE`: 数据库名称
- `MYSQL_USER`: 应用用户名
- `MYSQL_PASSWORD`: 应用密码

**后端**:
- `SPRING_DATASOURCE_URL`: 数据库连接 URL
- `SPRING_DATASOURCE_USERNAME`: 数据库用户名
- `SPRING_DATASOURCE_PASSWORD`: 数据库密码
- `SPRING_PROFILES_ACTIVE`: Spring 配置文件（dev/prod）

### 端口映射

如需修改端口映射，编辑 `docker-compose.yml` 中的 `ports` 配置：

```yaml
services:
  mysql:
    ports:
      - "3307:3306"  # 将 MySQL 映射到宿主机 3307 端口

  backend:
    ports:
      - "8081:8080"  # 将后端映射到宿主机 8081 端口

  frontend:
    ports:
      - "8080:80"    # 将前端映射到宿主机 8080 端口
```

## 生产环境部署

### 1. 使用外部 MySQL

修改 `docker-compose.yml`，移除 MySQL 服务并更新后端配置：

```yaml
services:
  backend:
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://your-mysql-host:3306/gsms
      SPRING_DATASOURCE_USERNAME: your-username
      SPRING_DATASOURCE_PASSWORD: your-password
```

### 2. 配置域名和 HTTPS

使用 Nginx 反向代理配置 HTTPS：

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location / {
        proxy_pass http://localhost:80;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
    }
}
```

### 3. 数据备份

定期备份 MySQL 数据：

```bash
# 备份
docker exec gsms-mysql mysqldump -uroot -proot123 gsms > backup.sql

# 恢复
docker exec -i gsms-mysql mysql -uroot -proot123 gsms < backup.sql
```

### 4. 日志管理

配置日志轮转，避免日志文件过大：

```yaml
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

## 故障排查

### 服务无法启动

```bash
# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs backend
```

### 数据库连接失败

1. 确认 MySQL 服务已就绪：
```bash
docker-compose logs mysql
```

2. 进入容器测试连接：
```bash
docker exec -it gsms-mysql mysql -ugsms -pgsms123 gsms
```

### 前端无法访问后端

1. 检查网络连接：
```bash
docker network inspect gsms-network
```

2. 确认后端健康状态：
```bash
curl http://localhost:8080/actuator/health
```

### 重新构建镜像

```bash
# 重新构建并启动
docker-compose up -d --build

# 清理旧镜像后重新构建
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

## 监控和维护

### 健康检查

```bash
# 检查所有服务健康状态
docker-compose ps

# 检查后端健康
curl http://localhost:8080/actuator/health
```

### 查看资源使用

```bash
# 查看容器资源使用情况
docker stats
```

### 清理未使用的资源

```bash
# 清理停止的容器
docker container prune

# 清理未使用的镜像
docker image prune

# 清理未使用的数据卷
docker volume prune
```

## 升级部署

```bash
# 1. 备份数据
docker exec gsms-mysql mysqldump -uroot -proot123 gsms > backup.sql

# 2. 拉取最新代码
git pull origin main

# 3. 重新构建并启动
docker-compose up -d --build

# 4. 验证服务
curl http://localhost:8080/actuator/health
```

## 安全建议

1. **修改默认密码**: 生产环境务必修改 MySQL root 密码和应用密码
2. **限制端口暴露**: 仅暴露必要的端口
3. **启用 HTTPS**: 使用 SSL/TLS 加密传输
4. **定期更新**: 及时更新 Docker 镜像和安全补丁
5. **日志审计**: 启用操作日志记录和审计
6. **网络隔离**: 使用 Docker 网络隔离不同服务

## 参考资源

- [Docker Compose 文档](https://docs.docker.com/compose/)
- [Spring Boot Docker 部署](https://spring.io/guides/topicals/spring-boot-docker/)
- [Nginx 配置指南](https://nginx.org/en/docs/)
