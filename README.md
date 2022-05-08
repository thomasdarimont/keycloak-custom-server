Custom Keycloak Server
----

Simple example for creating a custom Quarkus based Keycloak Distribution with 0 known CVEs.

# Features
- Create a custom Quarkus based Keycloak Distribution and Docker Image
- Support for using your own extensions and themes
- Support for removing unwanted Quarkus Extensions via maven dependency excludes
- Support for latest patch levels for libraries with known CVEs
- Support for secure Docker image based on alpine to avoid CVEs in base image. 

An [example for a Image scan with aqasec/trivy shows](https://gist.github.com/thomasdarimont/efb1a1327a585517db5a047401852a88) that this project can produce 
a custom Keycloak docker image with 0 known CVEs.

# Build

## Build custom distribution
```
mvn clean verify
```

## Build with Integration Tests
```
mvn clean verify -Pwith-integration-tests
```

## Build docker image
```
mvn clean verify docker:build
```

## Build docker image with Zero (known) CVEs

See [zero-cves Branch](https://github.com/thomasdarimont/keycloak-custom-server/tree/zero-cves) or perform the following steps yourself:
- Uncomment the h2 exclusions from the dependency section in the the pom.xml file.
- Uncomment the `db` setting in src/main/resources/META-INF/keycloak.conf and set an appropriate value, e.g. `postgres`.

```
mvn clean verify docker:build -Ddocker.file=keycloak/Dockerfile.alpin
```

# Scan

## Scan the image with Aquasec Trivy

Before running the command below, ensure that the custom keycloak docker image was build successfuly.

```
java bin/scanImage.java --verbose --image-name=thomasdarimont/custom-keycloakx:1.0.0-SNAPSHOT
```

# Run the custom distribution directly

The following example command shows how to run the custom Keycloak distribution against 
a postgres instance accessible on `127.0.0.1`.

```
target/keycloak-18.0.0/bin/kc.sh \
   start \
   --auto-build \
   --http-enabled=true \
   --http-relative-path=auth \
   --hostname-strict=false \
   --hostname-strict-https=false \
   --db=postgres \
   --db-url-host=127.0.0.1 \
   --db-url-database=keycloak \
   --db-username=keycloak \
   --db-password=keycloak
```

# Run the docker image

The following example command shows how to run the custom docker image against 
a postgres instance accessible on the docker host via `172.17.0.1` in this case.

```
docker run --rm -it \
    -p 8080:8080 \
    -e KEYCLOAK_ADMIN=keycloak \
    -e KEYCLOAK_ADMIN_PASSWORD=keycloak \
    thomasdarimont/custom-keycloakx:1.0.0-SNAPSHOT \
    start \
   --auto-build \
   --http-enabled=true \
   --http-relative-path=auth \
   --hostname-strict=false \
   --hostname-strict-https=false \
   --db=postgres \
   --db-url-host=172.17.0.1 \
   --db-url-database=keycloak \
   --db-username=keycloak \
   --db-password=keycloak
```
