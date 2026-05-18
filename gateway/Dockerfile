# 使用 Java 21 作为基础镜像
FROM eclipse-temurin:21-jre-alpine

# 设置维护者信息
LABEL maintainer="Vex-Owl Team"

# 设置工作目录
WORKDIR /app

# 复制构建好的 JAR 文件
COPY target/gateway-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
