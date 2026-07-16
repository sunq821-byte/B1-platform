ARG JDK_IMAGE=loongarch64/openjdk:21
FROM ${JDK_IMAGE}

WORKDIR /app
COPY target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
