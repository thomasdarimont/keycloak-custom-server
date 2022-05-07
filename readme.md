Custom Keycloak Server
----

Simple example for creating a custom Quarkus based Keycloak Distribution.

Unwanted features can be removed via maven dependency excludes.

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

# Run
```
target/keycloak-18.0.0/bin/kc.sh \
  start-dev \
  --db postgres \
  --db-url-host localhost \
  --db-username keycloak \
  --db-password keycloak \
  --http-port=8080 \
  --http-relative-path=auth \
  --spi-events-listener-jboss-logging-success-level=info \
  --spi-events-listener-jboss-logging-error-level=warn  \
  --https-certificate-file=../../../config/stage/dev/tls/acme.test+1.pem \
  --https-certificate-key-file=../../../config/stage/dev/tls/acme.test+1-key.pem
```

