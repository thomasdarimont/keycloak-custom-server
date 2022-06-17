package com.github.thomasdarimont.keycloak.support;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

/**
 * Starts a Keycloak container with a Keycloak-Config-CLI sidecar.
 */
public class KeycloakEnvironment {

    private static final Logger log = LoggerFactory.getLogger(KeycloakEnvironment.class);

    private KeycloakContainer keycloak;

    private GenericContainer<?> keycloakConfigCli;

    private boolean runConfigCli = true;

    public void start() {
        keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:18.0.1");
        keycloak.withProviderClassesFrom("target/classes");
        keycloak.withReuse(true);
        keycloak.withCreateContainerCmdModifier(cmd -> cmd.withName("keycloak-" + Long.toHexString(System.currentTimeMillis())));
        keycloak.start();
        keycloak.followOutput(new Slf4jLogConsumer(log));
        log.info("Keycloak started");

        if (runConfigCli) {
            keycloakConfigCli = createKeycloakConfigCliContainer(keycloak);
            keycloakConfigCli.start();
            keycloakConfigCli.followOutput(new Slf4jLogConsumer(log));
            log.info("Keycloak-Config-CLI started");
        }
    }

    public void stop() {
        if (keycloak != null) {
            keycloak.stop();
            log.info("Keycloak stopped");
        }

        if (keycloakConfigCli != null) {
            keycloakConfigCli.stop();
            log.info("Keycloak-Config-CLI stopped");
        }
    }

    public KeycloakContainer getKeycloak() {
        return keycloak;
    }

    public GenericContainer<?> getKeycloakConfigCli() {
        return keycloakConfigCli;
    }

    public Keycloak getAdminClient() {
        return keycloak.getKeycloakAdminClient();
    }

    public TokenService getTokenService() {
        return getClientProxy(TokenService.class);
    }

    public <T> T getClientProxy(Class<T> iface) {
        return iface.cast(getResteasyWebTarget(keycloak).proxy(iface));
    }

    private ResteasyWebTarget getResteasyWebTarget(KeycloakContainer keycloak) {
        return (ResteasyWebTarget) ClientBuilder.newBuilder().build().target(UriBuilder.fromPath(keycloak.getAuthServerUrl()));
    }

    public boolean isRunConfigCli() {
        return runConfigCli;
    }

    public void setRunConfigCli(boolean runConfigCli) {
        this.runConfigCli = runConfigCli;
    }


    public static GenericContainer<?> createKeycloakConfigCliContainer(KeycloakContainer keycloakContainer) {

        GenericContainer<?> keycloakConfigCli = new GenericContainer<>("quay.io/adorsys/keycloak-config-cli:5.2.0-18.0.0");
        keycloakConfigCli.addEnv("KEYCLOAK_AVAILABILITYCHECK_ENABLED", "true");
        keycloakConfigCli.addEnv("KEYCLOAK_AVAILABILITYCHECK_TIMEOUT", "30s");
        keycloakConfigCli.addEnv("IMPORT_FILES_LOCATION", "/config/*");
        keycloakConfigCli.addEnv("IMPORT_CACHE_ENABLED", "true");
        keycloakConfigCli.addEnv("IMPORT_VAR_SUBSTITUTION_ENABLED", "true");
        keycloakConfigCli.addEnv("KEYCLOAK_USER", keycloakContainer.getAdminUsername());
        keycloakConfigCli.addEnv("KEYCLOAK_PASSWORD", keycloakContainer.getAdminPassword());
        keycloakConfigCli.addEnv("KEYCLOAK_URL", keycloakContainer.getAuthServerUrl());
        keycloakConfigCli.addEnv("KEYCLOAK_FRONTEND_URL", keycloakContainer.getAuthServerUrl());

        // TODO make the realm config folder parameterizable
        keycloakConfigCli.addFileSystemBind("src/test/resources/realms", "/config", BindMode.READ_ONLY, SelinuxContext.SHARED);
        keycloakConfigCli.setWaitStrategy(Wait.forLogMessage(".*keycloak-config-cli running in.*", 1));
        keycloakConfigCli.setNetworkMode("host");

        keycloakConfigCli.withCreateContainerCmdModifier(cmd -> cmd.withName("keycloak-config-cli-" + Long.toHexString(System.currentTimeMillis())));

        return keycloakConfigCli;
    }
}
