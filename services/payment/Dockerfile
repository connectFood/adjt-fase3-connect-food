# ===== Build Stage =====
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -DskipTests clean package

# ===== Runtime Stage =====
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

COPY --from=builder /app/target/*jar /app/app.jar

EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
