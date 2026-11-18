
# Etapa 1: compilar a API
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN chmod +x mvnw && \
    ./mvnw --batch-mode --no-transfer-progress clean package -DskipTests

# Etapa 2: executar a API
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
