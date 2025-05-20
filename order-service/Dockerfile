FROM maven:3.9.9-amazoncorretto-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY common common/
COPY api-gateway api-gateway/
COPY auth-service auth-service/
COPY catalog-service catalog-service/
COPY shop-service shop-service/
COPY media-service media-service/
COPY order-service order-service/

RUN mvn -B -pl order-service -am clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/order-service/target/*.jar ./order-service.jar

EXPOSE 8084
ENTRYPOINT ["java","-jar","order-service.jar"]