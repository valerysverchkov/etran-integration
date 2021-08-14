FROM adoptopenjdk/openjdk11:alpine-jre
MAINTAINER Valerii Sverchkov "valerysverchkov@gmail.com"
ARG JAR_IMAGE=etran-integration-0.0.1-SNAPSHOT.jar
ARG JAR_FILE=build/libs/etran-integration-0.0.1-SNAPSHOT.jar
WORKDIR /opt/app
COPY ${JAR_FILE} ${JAR_IMAGE}
EXPOSE 8080
ENTRYPOINT ["java", "-Xms512m -Xmx1g -XX:+UseG1GC", "-jar", "calculator.jar"]