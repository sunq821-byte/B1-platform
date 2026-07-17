ARG JDK_IMAGE=lcr.loongnix.cn/library/openjdk:21
FROM ${JDK_IMAGE}

WORKDIR /app
COPY target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
