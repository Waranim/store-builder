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

RUN mvn -B -pl catalog-service -am clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/catalog-service/target/*.jar ./catalog-service.jar

EXPOSE 8083
ENTRYPOINT ["java","-jar","catalog-service.jar"]