FROM openjdk:11-jdk
MAINTAINER Netgrif <netgrif@netgrif.com>

RUN mkdir -p /src/main/

ARG JAR_FILE=target/*-exec.jar
ARG RESOURCE=src/main/resources

COPY ${RESOURCE} src/main/resources
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]

#ENTRYPOINT ["java","-Dspring.config.location=classpath:/,optional:file:/config/,optional:file:/config/*/","-jar","/app.jar"]