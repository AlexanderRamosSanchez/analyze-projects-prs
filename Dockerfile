# Etapa de construcción
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Variables de entorno para la configuración
ENV R2DBC_URL=${R2DBC_URL}
ENV R2DBC_USERNAME=${R2DBC_USERNAME}
ENV R2DBC_PASSWORD=${R2DBC_PASSWORD}
ENV KAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS}
ENV KAFKA_USERNAME=${KAFKA_USERNAME}
ENV KAFKA_PASSWORD=${KAFKA_PASSWORD}

# Exponer el puerto
EXPOSE 8081

# Comando de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]
