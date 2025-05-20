FROM maven:3.9.9-amazoncorretto-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY common common/
COPY api-gateway api-gateway/
COPY auth-service auth-service/
COPY order-service order-service/
COPY catalog-service catalog-service/
COPY shop-service shop-service/
COPY media-service media-service/

RUN mvn -B -pl media-service -am clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/media-service/target/*.jar ./media-service.jar

EXPOSE 8085
ENTRYPOINT ["java","-jar","media-service.jar"]