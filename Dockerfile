FROM openjdk:18-jdk

COPY target/spring.jar spring.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "spring.jar"]