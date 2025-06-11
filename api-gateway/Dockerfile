FROM maven:3.9.9-amazoncorretto-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY common common/
COPY auth-service auth-service/
COPY order-service order-service/
COPY catalog-service catalog-service/
COPY shop-service shop-service/
COPY media-service media-service/
COPY payment-service payment-service/
COPY notification-service notification-service/
COPY api-gateway api-gateway/

RUN mvn -B -pl api-gateway -am clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/api-gateway/target/*.jar ./api-gateway.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","api-gateway.jar"]