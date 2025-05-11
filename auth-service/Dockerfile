FROM eclipse-temurin:21-jre
ARG JAR_FILE=target/*SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources/*.pem src/main/resources/
ENTRYPOINT ["java","-jar","/app.jar"]
