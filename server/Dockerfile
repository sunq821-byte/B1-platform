ARG JDK_IMAGE=eclipse-temurin:21-jre
FROM ${JDK_IMAGE}

WORKDIR /app
COPY target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
