ARG JDK_IMAGE=loongarch64/openjdk:21
FROM ${JDK_IMAGE} AS build

RUN apt-get update && apt-get install -y maven
WORKDIR /src
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM ${JDK_IMAGE}

WORKDIR /app
COPY --from=build /src/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
