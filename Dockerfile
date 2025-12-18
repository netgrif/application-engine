FROM eclipse-temurin:21-jdk
LABEL authors="Netgrif <devops@netgrif.com>"
LABEL org.opencontainers.image.authors="NETGRIF <devops@netgrif.com>"
LABEL org.opencontainers.image.title="Netgrif Application Engine"
LABEL org.opencontainers.image.url="https://platform.netgrif.cloud"
LABEL org.opencontainers.image.documentation="https://platform.netgrif.cloud/docs"
LABEL org.opencontainers.image.vendor="NETGRIF"

RUN mkdir -p /opt/netgrif/engine
RUN mkdir -p /opt/netgrif/engine/modules
COPY application-engine/target/app-exec.jar /opt/netgrif/engine/app.jar
COPY application-engine/src/main/resources /opt/netgrif/engine/src/main/resources

WORKDIR /opt/netgrif/engine
EXPOSE 8080

ENTRYPOINT ["java","-Dfile.encoding=UTF-8","-jar","app.jar"]
