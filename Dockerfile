FROM adoptopenjdk/openjdk11:alpine-jre
MAINTAINER Valerii Sverchkov "valerysverchkov@gmail.com"
ARG JAR_IMAGE=etran-integration-1.0-SNAPSHOT.jar
ARG JAR_FILE=target/etran-integration-1.0-SNAPSHOT.jar
ENV JAVA_OPTS="-Xms128M -Xmx1024M"
WORKDIR /opt/app
COPY ${JAR_FILE} ${JAR_IMAGE}
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "etran-integration-1.0-SNAPSHOT.jar"]