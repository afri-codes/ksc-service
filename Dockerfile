# FROM openjdk:17-jdk
FROM eclipse-temurin:17-jdk
LABEL maintainer="jsc.go.tz"
COPY target/*.jar app.jar
ENV JAVA_OPTS=""
#EXPOSE 8761
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Djava.security.egd=file:/dev/.random -jar /app.jar"]