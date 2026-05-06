# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-24 AS builder
WORKDIR /build
COPY pom.xml ./
COPY scoring/pom.xml scoring/
COPY app/pom.xml app/
RUN mvn -B -q -e -ntp dependency:go-offline -DskipTests || true
COPY scoring/src scoring/src
COPY app/src app/src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:24-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /build/app/target/app-*.jar /app/app.jar
USER spring
EXPOSE 8087
ENTRYPOINT ["java","-jar","/app/app.jar"]
