# Construir la imagen base utilizando OpenJDK 18 y Maven
FROM openjdk:18-jdk-alpine as builder
WORKDIR /app

# Instalar Maven
RUN apk add --no-cache maven

COPY ./pom.xml ./pom.xml
COPY ./src ./src

RUN mvn clean package -DskipTests

# Utilizar una imagen de OpenJDK 18 para ejecutar la aplicación
FROM openjdk:18-jdk-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto en el que la aplicación se ejecutará
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
