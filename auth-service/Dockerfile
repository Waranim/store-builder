FROM maven:3.9.9-amazoncorretto-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY common common/
COPY api-gateway api-gateway/
COPY order-service order-service/
COPY catalog-service catalog-service/
COPY shop-service shop-service/
COPY media-service media-service/
COPY payment-service payment-service/
COPY notification-service notification-service/
COPY auth-service auth-service/

RUN mvn -B -pl auth-service -am clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/auth-service/target/*.jar ./auth-service.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","auth-service.jar"]