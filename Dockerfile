# 1. 基础环境：使用轻量级的 Java 17 镜像
FROM eclipse-temurin:17-jre-alpine

# 2. 设置工作目录
WORKDIR /app

# 3. 将本地编译好的 jar 包复制进集装箱
COPY target/*.jar app.jar

# 4. 创建上传文件夹的挂载点
VOLUME /app/uploads

# 5. 声明集装箱对外暴露 8080 端口
EXPOSE 8080

# 6. 点火启动命令（开机自动运行 jar 包）
ENTRYPOINT ["java", "-jar", "app.jar"]