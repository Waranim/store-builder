FROM maven:3.9.9-amazoncorretto-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY common common/
COPY api-gateway api-gateway/
COPY auth-service auth-service/
COPY order-service order-service/
COPY catalog-service catalog-service/
COPY media-service media-service/
COPY shop-service shop-service/

RUN mvn -B -pl shop-service -am clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/shop-service/target/*.jar ./shop-service.jar

EXPOSE 8082
ENTRYPOINT ["java","-jar","shop-service.jar"]