FROM maven:3.9.9-amazoncorretto-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY common common/
COPY api-gateway api-gateway/
COPY auth-service auth-service/
COPY order-service order-service/
COPY shop-service shop-service/
COPY media-service media-service/
COPY catalog-service catalog-service/
COPY notification-service notification-service/
COPY payment-service payment-service/

RUN mvn -B -pl payment-service -am clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/payment-service/target/*.jar ./payment-service.jar

EXPOSE 8086
ENTRYPOINT ["java","-jar","payment-service.jar"]