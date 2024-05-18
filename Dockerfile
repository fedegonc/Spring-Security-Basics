# Usar una imagen de base de Maven con JDK 18 para construir la aplicación
FROM maven:3.9.2-openjdk-18 as builder
WORKDIR /app

COPY ./pom.xml ./pom.xml
COPY ./src ./src

RUN mvn clean package -DskipTests

# Usar una imagen de base de OpenJDK 18 para ejecutar la aplicación
FROM openjdk:18-jdk-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto en el que la aplicación se ejecutará
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
