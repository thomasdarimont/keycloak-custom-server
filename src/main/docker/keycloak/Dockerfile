FROM registry.access.redhat.com/ubi8-minimal:8.5 AS builder

RUN microdnf update -y && \
    microdnf install -y java-17-openjdk-headless && microdnf clean all && rm -rf /var/cache/yum/* && \
    echo "keycloak:x:0:root" >> /etc/group && \
    echo "keycloak:x:1000:0:keycloak user:/opt/keycloak:/sbin/nologin" >> /etc/passwd

COPY --chown=keycloak:keycloak maven/keycloak  /opt/keycloak

USER 1000

RUN /opt/keycloak/bin/kc.sh build

FROM registry.access.redhat.com/ubi8-minimal:8.5

RUN microdnf update -y && \
    microdnf reinstall -y tzdata && \
    microdnf install -y java-17-openjdk-headless && \
    microdnf clean all && rm -rf /var/cache/yum/* && \
    echo "keycloak:x:0:root" >> /etc/group && \
    echo "keycloak:x:1000:0:keycloak user:/opt/keycloak:/sbin/nologin" >> /etc/passwd && \
    ln -sf /usr/share/zoneinfo/Europe/Berlin /etc/localtime # set timezone

COPY --from=builder --chown=1000:0 /opt/keycloak /opt/keycloak

RUN echo "Add certificates to cacerts truststore" \
    && ls -l /opt/keycloak/conf/certs/* \
    && cp /opt/keycloak/conf/certs/*.pem /etc/pki/ca-trust/source/anchors \
    && echo "Updating cacerts truststore..."\
    && update-ca-trust \
    && echo "Certificates added to cacerts truststore" \
    || echo "Could not import certificates"

# Note you can check if certs were added with the following commands
# keytool -list -keystore /etc/pki/java/cacerts -storepass changeit |& head
# keytool -list -keystore /etc/pki/java/cacerts -storepass changeit |& grep -i dbtrustcenterroot

USER 1000

EXPOSE 8080
EXPOSE 8443

ENTRYPOINT [ "/opt/keycloak/bin/kc.sh" ]