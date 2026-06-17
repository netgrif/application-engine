# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jre

ARG VERSION="7.0.0"

LABEL authors="Netgrif <devops@netgrif.com>" \
      org.opencontainers.image.authors="NETGRIF <devops@netgrif.com>" \
      org.opencontainers.image.documentation="https://platform.netgrif.cloud/docs" \
      org.opencontainers.image.title="Netgrif Application Engine" \
      org.opencontainers.image.url="https://platform.netgrif.cloud" \
      org.opencontainers.image.vendor="NETGRIF" \
      org.opencontainers.image.version="${VERSION}"

ARG NETGRIF_UID=10001
ARG NETGRIF_GID=10001

WORKDIR /opt/netgrif/engine

RUN groupadd --gid "${NETGRIF_GID}" netgrif \
    && useradd --uid "${NETGRIF_UID}" \
        --gid netgrif \
        --home-dir /nonexistent \
        --no-create-home \
        --shell /usr/sbin/nologin \
        netgrif \
    && install -d -o netgrif -g netgrif -m 0750 \
        /opt/netgrif/engine/modules \
        /opt/netgrif/engine/storage \
        /opt/netgrif/engine/log

COPY --chown=0:0 --chmod=0444 application-engine/target/app-exec.jar app.jar
COPY --chown=0:0 application-engine/src/main/resources src/main/resources

RUN find src/main/resources -type d -exec chmod 0555 {} + \
    && find src/main/resources -type f -exec chmod 0444 {} +

USER ${NETGRIF_UID}:${NETGRIF_GID}

EXPOSE 8080
STOPSIGNAL SIGTERM

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
