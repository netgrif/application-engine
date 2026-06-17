# Netgrif Application Engine

[![License](https://img.shields.io/badge/license-NETGRIF%20Community%20License-green)](https://netgrif.com/license)
[![Java](https://img.shields.io/badge/Java-21-red)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Petriflow 1.0.8](https://img.shields.io/badge/Petriflow-1.0.8-0aa8ff)](https://petriflow.org)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/netgrif/application-engine?sort=semver&display_name=tag)](https://github.com/netgrif/application-engine/releases)
[![build](https://github.com/netgrif/application-engine/actions/workflows/master-build.yml/badge.svg)](https://github.com/netgrif/application-engine/actions/workflows/master-build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=netgrif_application-engine&metric=alert_status)](https://sonarcloud.io/dashboard?id=netgrif_application-engine)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=netgrif_application-engine&metric=coverage)](https://sonarcloud.io/dashboard?id=netgrif_application-engine)
[![Known Vulnerabilities](https://snyk.io/test/github/netgrif/application-engine/badge.svg)](https://snyk.io/test/github/netgrif/application-engine)
[![Maven Central](https://img.shields.io/maven-central/v/com.netgrif/application-engine)](https://central.sonatype.com/artifact/com.netgrif/application-engine)
[![Docker Pulls](https://img.shields.io/docker/pulls/netgrif/application-engine)](https://hub.docker.com/r/netgrif/application-engine)

> Next-generation end-to-end low-code workflow platform built on Spring Boot and Petriflow.

**Netgrif Application Engine (NAE)** is an open-source workflow management system that fully supports
the [Petriflow](https://petriflow.org) low-code language. It is built on top of the Spring Framework and provides a
fully compliant Petriflow language interpreter along with a comprehensive set of enterprise-grade features including
user management, role-based access control, data management, full-text search, file storage, scheduled tasks, email
notifications, and PDF generation.

NAE runs inside the Java Virtual Machine (JVM) and can either be used as a **standalone process server** or **embedded
as a library** into any Spring Boot application.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture & Modules](#architecture--modules)
- [Requirements](#requirements)
- [Quick Start](#quick-start)
    - [Running as a JAR](#running-as-a-jar)
    - [Running with Docker](#running-with-docker)
    - [Running with Docker Compose](#running-with-docker-compose)
- [Embedding as a Library](#embedding-as-a-library)
- [Configuration](#configuration)
- [Development Setup](#development-setup)
- [CI/CD Workflows](#cicd-workflows)
- [Related Projects](#related-projects)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

Netgrif Application Engine provides a full-stack runtime for low-code process-driven applications modelled in
the [Petriflow](https://petriflow.org) language. Its process interpreter executes Petri-net-based workflows, manages
data fields, evaluates role permissions, fires events, and executes server-side Groovy action code — all at runtime
without redeployment.

- 🌐 **Platform:** [https://platform.netgrif.cloud](https://platform.netgrif.cloud)
- 📖 **Documentation:** [https://engine.netgrif.com](https://engine.netgrif.com)
- 🐛 **Issue Tracker:** [GitHub Issues](https://github.com/netgrif/application-engine/issues)
- ☕ **Javadoc:** [https://engine.netgrif.com/javadoc](https://engine.netgrif.com/javadoc)
- 📦 **Maven Central:
  ** [com.netgrif:application-engine](https://central.sonatype.com/artifact/com.netgrif/application-engine)
- 🐳 **Docker Hub:** [netgrif/application-engine](https://hub.docker.com/r/netgrif/application-engine)

---

## Features

### Workflow Engine

- **Petriflow interpreter** — Full support for the Petriflow low-code language (Petri-net-based processes)
- **Process & case management** — Create, manage, and track process instances (cases) and tasks
- **Actions & events** — Compile and execute server-side Groovy action code triggered by process events
- **Role management & permission resolution** — Fine-grained role-based access control per process, task, and data field
- **Dynamic data fields** — Rich set of field types: text, number, date, enumeration, file, user, task reference, and
  more

### Search & Indexing

- **Elasticsearch integration** — Full-text search and advanced filtering across cases and tasks
- **QueryDSL** — Type-safe MongoDB and Elasticsearch queries
- **Filter management** — Persistent user-defined search filters

### Authentication & Authorization

- **JWT-based authentication** — Stateless REST API security with RSA-signed tokens
- **LDAP / Active Directory** — Enterprise directory integration via Spring LDAP
- **Redis session management** — Distributed session store for scalable deployments
- **Open/closed registration** — Configurable user self-registration
- **Impersonation** — Admin user impersonation support
- **Rate limiting** — Login attempt throttling and email rate limiting

### Storage

- **MongoDB** — Primary document store for process definitions, cases, tasks, users
- **MinIO / S3-compatible** — Object storage for uploaded files and attachments (local file system as fallback/default
  storage)

### Integrations & Services

- **Mail service** — SMTP email client with FreeMarker HTML templates for registration and password-reset flows
- **PDF generator** — Generate PDF documents from process task forms, including SVG/image support
- **QR code generator** — Generate QR codes from process data using ZXing
- **Quartz scheduler** — Persistent, MongoDB-backed job scheduling
- **OpenAPI / Swagger** — Auto-generated REST API documentation via SpringDoc

### Developer Experience

- **Spring Boot auto-configuration** — Drop-in library embedding with `spring.factories`
- **Groovy scripting** — Dynamic action code execution without recompilation
- **Actuator** — Production-ready health, metrics, and management endpoints

---

## Architecture & Modules

The project is a Maven multi-module build composed of the following modules:

| Module                    | Artifact ID               | Description                                                                                              |
|---------------------------|---------------------------|----------------------------------------------------------------------------------------------------------|
| `nae-object-library`      | `nae-object-library`      | Core domain model: shared Petriflow objects, data field types, and value objects used across all modules |
| `nae-spring-core-adapter` | `nae-spring-core-adapter` | Spring-specific adapters and infrastructure abstractions (repositories, services, configuration)         |
| `nae-user-common`         | `nae-user-common`         | Common user domain model and interfaces shared between user management implementations                   |
| `nae-user-ce`             | `nae-user-ce`             | Community Edition user management module — user, role, and organisation management                       |
| `application-engine`      | `application-engine`      | Main Spring Boot application — assembles all modules, REST API, configuration, and process runtime       |

### Dependency Graph

```text
application-engine
  └── nae-user-ce
        └── nae-user-common
              └── nae-spring-core-adapter
                    └── nae-object-library
```

---

## Requirements

The following infrastructure services are required to run the Application Engine:

| Service                                                | Version | Purpose                 | Port        |
|--------------------------------------------------------|---------|-------------------------|-------------|
| [Java JDK](https://openjdk.java.net/)                  | 21+     | Runtime environment     | —           |
| [MongoDB](https://www.mongodb.com/)                    | 8+      | Primary document store  | 27017       |
| [Elasticsearch](https://www.elastic.co/elasticsearch/) | 8+      | Full-text search index  | 9200 / 9300 |
| [Redis](https://redis.io/)                             | 8+      | Session store & caching | 6379        |
| [MinIO](https://min.io/) *(optional)*                  | —       | File / object storage   | 9000 / 9001 |

> **Note:** MongoDB must be configured as a **Replica Set** (even a single-node `rs0`) because NAE uses MongoDB's
> trasanctions (for more see [MongoDB Docs on transactions](https://docs.mongodb.com/manual/core/transactions/)).
> See the provided `docker-compose.yml` for a ready-to-use local setup.

---

## Quick Start

### Prerequisites

Generate an RSA key pair required for JWT token signing before first startup:

```shell
cd application-engine/src/main/resources/certificates
openssl genrsa -out keypair.pem 4096
openssl rsa -in keypair.pem -pubout -out public.crt
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in keypair.pem -out private.der
cd ../../../../..
```

---

### Running as a JAR

Download the latest release package
from [GitHub Releases](https://github.com/netgrif/application-engine/releases/latest), unzip, generate security keys,
and start:

```shell
wget -O nae.zip https://github.com/netgrif/application-engine/releases/latest
unzip nae.zip
cd netgrif-application-engine-<version>
# Generate security keys (see Prerequisites above)
cd src/main/resources/certificates
openssl genrsa -out keypair.pem 4096
openssl rsa -in keypair.pem -pubout -out public.crt
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in keypair.pem -out private.der
cd ../../../..
java -jar app-exec.jar
```

Override database connection settings via command-line arguments:

```shell
java -jar app-exec.jar \
  --netgrif.engine.data.mongodb.uri=mongodb://localhost:27017 \
  --netgrif.engine.data.elasticsearch.url=localhost \
  --netgrif.engine.data.redis.host=localhost
```

Or use environment variables:

```shell
export NETGRIF_ENGINE_DATA_MONGODB_URI=mongodb://mongo-host:27017
export NETGRIF_ENGINE_DATA_ELASTIC_URL=elastic-host
export NETGRIF_ENGINE_DATA_REDIS_HOST=redis-host
java -jar app-exec.jar
```

---

### Running with Docker

Pull and run the official image from Docker Hub:

```shell
docker pull netgrif/application-engine
docker run -d \
  -p 8080:8080 \
  -e NETGRIF_ENGINE_DATA_MONGODB_URI=mongodb://host.docker.internal:27017 \
  -e NETGRIF_ENGINE_DATA_ELASTIC_URL=host.docker.internal \
  -e NETGRIF_ENGINE_DATA_REDIS_HOST=host.docker.internal \
  netgrif/application-engine
```

The engine is available at `http://localhost:8080`.

**Available tags:**

- `netgrif/application-engine:latest` — latest stable release
- `netgrif/application-engine:<version>` — specific version (e.g. `7.0.0`)

The Docker image supports both `linux/amd64` and `linux/arm64` platforms.

---

### Running with Docker Compose

The repository ships with a `docker-compose.yml` that starts all required
infrastructure services:

```shell
docker-compose up -d
```

This starts:

- **MongoDB 8.0** (with replica set `rs0`) on port `27017`
- **Elasticsearch 8.15** (single-node, security disabled) on ports `9200` / `9300`
- **Redis 8** on port `6379`
- **MinIO** (object storage) on ports `9000` / `9001`

Then start the engine:

```shell
java -jar target/app-exec.jar
```

---

## Embedding as a Library

NAE can be embedded into any existing Spring Boot application as a Maven dependency. Add the following to your
`pom.xml`:

```xml

<dependency>
    <groupId>com.netgrif</groupId>
    <artifactId>application-engine</artifactId>
    <version>7.0.0</version>
</dependency>
```

NAE uses Spring Boot auto-configuration (`spring.factories`) and will self-configure when placed on the classpath.
Extend, override, or replace any Spring bean to customize behaviour.

**Maven Central snapshot repository** (for pre-release versions):

```xml

<repositories>
    <repository>
        <name>Central Snapshots</name>
        <id>central-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

---

## Configuration

All engine configuration is done through `application.yaml` (or environment variables). Key configuration namespaces:

| Namespace                                        | Environment Variable                             | Default                                            | Description               |
|--------------------------------------------------|--------------------------------------------------|----------------------------------------------------|---------------------------|
| `netgrif.engine.data.mongodb.uri`                | `NETGRIF_ENGINE_DATA_MONGODB_URI`                | `mongodb://localhost:27017`                        | MongoDB connection URI    |
| `netgrif.engine.data.database-name`              | `NETGRIF_ENGINE_DATA_DATABASE_NAME`              | `nae`                                              | MongoDB database name     |
| `netgrif.engine.data.elasticsearch.url`          | `NETGRIF_ENGINE_DATA_ELASTIC_URL`                | `localhost`                                        | Elasticsearch host        |
| `netgrif.engine.data.elasticsearch.searchPort`   | `NETGRIF_ENGINE_DATA_ELASTIC_SEARCH_PORT`        | `9200`                                             | Elasticsearch HTTP port   |
| `netgrif.engine.data.redis.host`                 | `NETGRIF_ENGINE_DATA_REDIS_HOST`                 | `localhost`                                        | Redis host                |
| `netgrif.engine.data.redis.port`                 | `NETGRIF_ENGINE_DATA_REDIS_PORT`                 | `6379`                                             | Redis port                |
| `netgrif.engine.mail.host`                       | `NETGRIF_ENGINE_MAIL_HOST`                       | *(empty)*                                          | SMTP server host          |
| `netgrif.engine.mail.port`                       | `NETGRIF_ENGINE_MAIL_PORT`                       | `25`                                               | SMTP server port          |
| `netgrif.engine.security.auth.admin-password`    | `NETGRIF_ENGINE_SECURITY_AUTH_ADMIN_PASSWORD`    | `password`                                         | Default admin password    |
| `netgrif.engine.security.auth.open-registration` | `NETGRIF_ENGINE_SECURITY_AUTH_OPEN_REGISTRATION` | `true`                                             | Allow self-registration   |
| `netgrif.engine.security.jwt.private-key`        | —                                                | `file:src/main/resources/certificates/private.der` | RSA private key for JWT   |
| `netgrif.engine.server.port`                     | —                                                | `8080`                                             | HTTP server port          |
| `netgrif.engine.impersonation.enabled`           | `NETGRIF_ENGINE_IMPERSONATION_ENABLED`           | `true`                                             | Enable user impersonation |

> ⚠️ **Security note:** Change the default `netgrif.engine.security.auth.admin-password` and
`netgrif.engine.security.encryption.password` properties values before deploying to any
> non-development environment.

Full configuration
reference: [https://platform.netgrif.cloud/refs/config/engine-standalone-config](https://platform.netgrif.cloud/refs/config/engine-standalone-config)

---

## Development Setup

### Prerequisites

- JDK 21+
- Maven 3.9+
- Docker & Docker Compose (for infrastructure services)

### Steps

**1. Clone the repository**

```shell
git clone https://github.com/netgrif/application-engine.git
cd application-engine
```

**2. Start infrastructure services**

```shell
cd application-engine
docker-compose up -d
cd ..
```

**3. Generate RSA certificates**

```shell
cd application-engine/src/main/resources/certificates
openssl genrsa -out keypair.pem 4096
openssl rsa -in keypair.pem -pubout -out public.crt
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in keypair.pem -out private.der
cd ../../../../..
```

**4. Build the full multi-module project**

```shell
mvn clean install
```

**5. Link the `nae-user-ce` module for development hot-reload**

```shell
bash -c 'jar_path=$(find nae-user-ce/target/ -maxdepth 1 -type f -name "nae-user-ce-*.jar" \
  ! -name "*-javadoc.jar" ! -name "*-sources.jar" | head -n1) && \
  [[ -n "$jar_path" ]] && \
  cd application-engine/modules && \
  ln -sf ../../"$jar_path" nae-user-ce.jar && \
  echo "✅ Symlink created → application-engine/modules/nae-user-ce.jar" || \
  echo "❌ JAR not found!"'
```

**6. Run in development mode**

In IntelliJ IDEA, use the pre-configured run configuration **"ApplicationEngine DEV with modules"** (located in
`.run/`), which uses `org.springframework.boot.loader.launch.PropertiesLauncher` with the `dev` Spring profile. This
loads all JARs from `application-engine/modules/`, including the symlinked `nae-user-ce.jar`.

Alternatively, from the command line:

```shell
mvn -pl application-engine -P dev spring-boot:run
```

**7. Incremental rebuild of the engine module only**

```shell
mvn -pl application-engine -P dev clean install
```

### Running Tests

Tests require all infrastructure services to be running (MongoDB, Elasticsearch, Redis, MinIO):

```shell
mvn clean verify
```

---

## Related Projects

| Project                 | Description                                                             | Link                                                     |
|-------------------------|-------------------------------------------------------------------------|----------------------------------------------------------|
| **Netgrif Components**  | Angular frontend component library for NAE-powered applications         | [GitHub](https://github.com/netgrif/components)          |
| **Application Builder** | Visual drag-and-drop Petriflow process designer (also imports BPMN 2.0) | [builder.netgrif.com](https://builder.netgrif.com)       |
| **Petriflow**           | The open low-code language specification for process modelling          | [petriflow.org](https://petriflow.org)                   |
| **NETGRIF Platform**    | Managed cloud platform running on top of NAE                            | [platform.netgrif.cloud](https://platform.netgrif.cloud) |

---

## Contributing

We welcome contributions of all kinds — bug reports, feature requests, documentation improvements, and pull requests.

Please read our [Contributing Guide](CONTRIBUTING.md) and [Code of Conduct](CODE_OF_CONDUCT.md) before getting started.

- **Report a bug:** [Open an issue](https://github.com/netgrif/application-engine/issues/new)
- **Request a feature:** [Open an issue](https://github.com/netgrif/application-engine/issues/new) with a description of
  the problem and proposed solution
- **Submit a PR:** Fork the repository, create a feature branch, and open a pull request against `dev` branch

---

## License

Netgrif Application Engine is licensed under the **NETGRIF Community License**.

See the [LICENSE](https://github.com/netgrif/application-engine/blob/master/LICENSE.txt) file for the full license text.

---

<p align="center">
  Made by <a href="https://netgrif.com">NETGRIF, s.r.o.</a><br/>
  <a href="mailto:oss@netgrif.com">oss@netgrif.com</a>
</p>
