# Usar una imagen de base de Maven con JDK 18 para construir la aplicación
FROM maven:3.8.4-openjdk-18 AS build
WORKDIR /app

# Copiar los archivos de la aplicación y compilar
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Usar una imagen de base de OpenJDK para ejecutar la aplicación
FROM openjdk:18-jdk-slim
WORKDIR /app

# Copiar el archivo JAR compilado desde la etapa anterior
COPY --from=build /app/target/spring.jar app.jar

# Exponer el puerto en el que la aplicación se ejecutará
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
