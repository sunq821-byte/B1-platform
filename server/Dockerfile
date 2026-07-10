FROM openjdk:21

WORKDIR /app

# 预构建产物：开发机 mvn package 产出的 fat jar
COPY target/b1-platform-1.0.0-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

# profile 由 compose 的 SPRING_PROFILES_ACTIVE=docker 注入
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
