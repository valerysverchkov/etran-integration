FROM artifactory.devops.dev.local/ci-docker-local/dev-base/maven-gpn:latest as MAVEN

COPY pom.xml /build/
COPY src /build/src/

WORKDIR /build/
RUN mvn package

FROM artifactory.devops.dev.local/ci-docker-local/dev-base/openjdk-11-rhel7-gpn:latest

ENV APP etran-integration-1.0.jar

RUN mkdir /opt/app
WORKDIR /opt/app
COPY --from=MAVEN /build/target/${APP} /opt/app

EXPOSE 8080
CMD java -jar /opt/app/${APP}