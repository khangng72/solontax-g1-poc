FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY solontax-g1-management-core/pom.xml solontax-g1-management-core/
COPY solontax-g1-management-adapter/pom.xml solontax-g1-management-adapter/
COPY . .

RUN mvn clean package -DskipTests -pl solontax-g1-management-adapter -am

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/solontax-g1-management-adapter/target/solontax-g1-management-adapter-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 1975

ENTRYPOINT ["java", "-jar", "app.jar"]
