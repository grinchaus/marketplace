FROM eclipse-temurin:21-jre
ARG JAR=target/*.jar
COPY ${JAR} /app.jar
COPY config-repo /config-repo
ENTRYPOINT ["java","-jar","/app.jar"]