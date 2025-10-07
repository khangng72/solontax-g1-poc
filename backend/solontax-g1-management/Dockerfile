FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./
COPY solontax-g1-management/pom.xml ./solontax-g1-management/

COPY solontax-g1-management/src ./solontax-g1-management/src/

RUN mvn clean package -DskipTests -pl solontax-g1-management -am

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/solontax-g1-management/target/solontax-g1-management-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 1975

ENTRYPOINT ["java", "-jar", "app.jar"]
