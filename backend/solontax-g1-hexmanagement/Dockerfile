FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./
COPY solontax-g1-hexmanagement/pom.xml ./solontax-g1-hexmanagement/

COPY solontax-g1-hexmanagement/src ./solontax-g1-hexmanagement/src/

RUN mvn clean package -DskipTests -pl solontax-g1-hexmanagement -am

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/solontax-g1-hexmanagement/target/solontax-g1-hexmanagement-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 1975

ENTRYPOINT ["java", "-jar", "app.jar"]
