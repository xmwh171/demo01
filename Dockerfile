# 基础镜像（轻量JDK17）
FROM openjdk:17-alpine
# 作者信息
MAINTAINER xiaohu <xiaohu@1209325963@qq.com>
# 设定工作目录
WORKDIR /app
# 复制打包后的jar包到容器（注意：需先执行mvn clean package打包）
COPY target/springboot-demo-0.0.1-SNAPSHOT.jar app.jar
# 暴露端口（和项目配置的server.port一致）
EXPOSE 8080
# 启动命令（指定生产环境配置）
ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=prod"]