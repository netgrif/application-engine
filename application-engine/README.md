# application-engine

> Main Spring Boot application module of the [Netgrif Application Engine](../README.md).

This module is the runnable assembly of the NAE platform. It wires together all sibling modules
(`nae-user-ce`, `nae-spring-core-adapter`, `nae-user-common`, `nae-object-library`), provides the
REST API, Spring Boot auto-configuration, and the Petriflow process runtime. It can be run as a
**standalone JAR / Docker container** or embedded as a **library** inside another Spring Boot application.

---

## Module coordinates

```xml

<dependency>
    <groupId>com.netgrif</groupId>
    <artifactId>application-engine</artifactId>
    <version>7.0.0</version>
</dependency>
```

---

## Requirements

| Service                 | Version | Port        |
|-------------------------|---------|-------------|
| Java JDK                | 21+     | —           |
| MongoDB *(replica set)* | 8+      | 27017       |
| Elasticsearch           | 8+      | 9200 / 9300 |
| Redis                   | 7+      | 6379        |
| MinIO *(optional)*      | —       | 9000 / 9001 |

Start all services locally with the provided Docker Compose file:

```shell
docker-compose up -d
```

---

## Build

Build this module alone (after a full root `mvn install`):

```shell
# standard JAR
mvn clean package

# Docker-ready fat JAR (output: target/app-exec.jar)
mvn clean package -P docker-build
```

---

## Run

### Prerequisites — RSA key pair for JWT

```shell
cd src/main/resources/certificates
openssl genrsa -out keypair.pem 4096
openssl rsa -in keypair.pem -pubout -out public.crt
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in keypair.pem -out private.der
cd ../../../..
```

### JAR

```shell
java -jar target/app-exec.jar
```

Override infrastructure URLs at startup:

```shell
java -jar target/app-exec.jar \
--netgrif.engine.data.mongodb.uri=mongodb://localhost:27017 \
--netgrif.engine.data.elasticsearch.url=localhost \
--netgrif.engine.data.redis.host=localhost
```

Or via environment variables:

```shell
export NETGRIF_ENGINE_DATA_MONGODB_URI=mongodb://localhost:27017
export NETGRIF_ENGINE_DATA_ELASTIC_URL=localhost
export NETGRIF_ENGINE_DATA_REDIS_HOST=localhost
java -jar target/app-exec.jar
```

The engine starts on **http://localhost:8080** by default.

### Docker

```shell
docker pull netgrif/application-engine
docker run -d -p 8080:8080 \
-e NETGRIF_ENGINE_DATA_MONGODB_URI=mongodb://host.docker.internal:27017 \
-e NETGRIF_ENGINE_DATA_ELASTIC_URL=host.docker.internal \
-e NETGRIF_ENGINE_DATA_REDIS_HOST=host.docker.internal \
netgrif/application-engine
```

---

## Key configuration properties

| Property                                       | Env variable                                  | Default                                            | Description             |
|------------------------------------------------|-----------------------------------------------|----------------------------------------------------|-------------------------|
| `netgrif.engine.data.mongodb.uri`              | `NETGRIF_ENGINE_DATA_MONGODB_URI`             | `mongodb://localhost:27017`                        | MongoDB URI             |
| `netgrif.engine.data.database-name`            | `NETGRIF_ENGINE_DATA_DATABASE_NAME`           | `nae`                                              | Database name           |
| `netgrif.engine.data.elasticsearch.url`        | `NETGRIF_ENGINE_DATA_ELASTIC_URL`             | `localhost`                                        | Elasticsearch host      |
| `netgrif.engine.data.elasticsearch.searchPort` | `NETGRIF_ENGINE_DATA_ELASTIC_SEARCH_PORT`     | `9200`                                             | Elasticsearch HTTP port |
| `netgrif.engine.data.redis.host`               | `NETGRIF_ENGINE_DATA_REDIS_HOST`              | `localhost`                                        | Redis host              |
| `netgrif.engine.data.redis.port`               | `NETGRIF_ENGINE_DATA_REDIS_PORT`              | `6379`                                             | Redis port              |
| `netgrif.engine.security.auth.admin-password`  | `NETGRIF_ENGINE_SECURITY_AUTH_ADMIN_PASSWORD` | `password`                                         | Default admin password  |
| `netgrif.engine.security.jwt.private-key`      | —                                             | `file:src/main/resources/certificates/private.der` | RSA private key path    |
| `netgrif.engine.server.port`                   | —                                             | `8080`                                             | HTTP server port        |

> ⚠️ Always change the default `admin-password` and `security.encryption.password` before any non-development
> deployment.

Full configuration
reference: [https://platform.netgrif.cloud/refs/config/engine-standalone-config](https://platform.netgrif.cloud/refs/config/engine-standalone-config)

---

## Maven build profiles

| Profile                 | Purpose                                                                         |
|-------------------------|---------------------------------------------------------------------------------|
| `dev`                   | Development mode — loads external JARs from `modules/` via `PropertiesLauncher` |
| `docker-build`          | Produces `app-exec.jar` used by the `Dockerfile`                                |
| `github-publish`        | Publishes to GitHub Packages                                                    |
| `ossrh-publish`         | Publishes to Maven Central                                                      |
| `netgrif-nexus-publish` | Publishes to internal Netgrif Nexus                                             |

---

## Development mode (IntelliJ / CLI)

Use the pre-configured run configuration **"ApplicationEngine DEV with modules"** in `.run/`,
or from the command line:

```shell
# From repository root — link nae-user-ce into the modules/ directory first
bash -c 'jar_path=$(find nae-user-ce/target/ -maxdepth 1 -type f -name "nae-user-ce-*.jar" \
! -name "*-javadoc.jar" ! -name "*-sources.jar" | head -n1) && \
[[ -n "$jar_path" ]] && \
cd application-engine/modules && ln -sf ../../"$jar_path" nae-user-ce.jar'

# Then run with the dev profile
mvn -pl application-engine -P dev spring-boot:run
```

---

## License

[NETGRIF Community License](../LICENSE.txt)
