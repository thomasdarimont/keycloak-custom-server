package com.github.thomasdarimont.keycloak;

import com.github.thomasdarimont.keycloak.support.KeycloakEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.TokenVerifier;
import org.keycloak.representations.IDToken;
import org.testcontainers.containers.output.ToStringConsumer;

import javax.ws.rs.core.Form;
import java.util.concurrent.TimeUnit;

import static com.github.thomasdarimont.keycloak.support.KeycloakTestSupport.createOrUpdateTestUser;
import static org.assertj.core.api.Assertions.assertThat;

public class KeycloakIntegrationTest {

    public static final String TEST_REALM_NAME = "testing";
    public static final String TEST_USER_USERNAME = "tester";
    public static final String TEST_USER_PASSWORD = "test";
    public static final String TEST_CLIENT_CLIENT_ID = "acme-client-legacy-app";

    private static final KeycloakEnvironment KEYCLOAK_ENVIRONMENT = new KeycloakEnvironment();

    @BeforeAll
    public static void beforeAll() {
        KEYCLOAK_ENVIRONMENT.start();
    }

    @AfterAll
    public static void afterAll() {
        KEYCLOAK_ENVIRONMENT.stop();
    }

    @Test
    public void obtainIdToken() throws Exception {

        var keycloakAdminClient = KEYCLOAK_ENVIRONMENT.getAdminClient();

        var acmeRealm = keycloakAdminClient.realm(TEST_REALM_NAME);

        var dynamicUser = createOrUpdateTestUser(acmeRealm, "dynamic-test-user", TEST_USER_PASSWORD, user -> {
            user.setFirstName("Firstname");
            user.setLastName("Lastname");
        });

        var tokenService = KEYCLOAK_ENVIRONMENT.getTokenService();

        var accessTokenResponse = tokenService.grantToken(TEST_REALM_NAME, new Form()
                .param("grant_type", "password")
                .param("username", dynamicUser.getUsername())
                .param("password", TEST_USER_PASSWORD)
                .param("client_id", TEST_CLIENT_CLIENT_ID)
                .param("scope", "openid profile")
                .asMap());

//            System.out.println("Token: " + accessTokenResponse.getToken());

        // parse the received id-token
        var tokenVerifier = TokenVerifier.create(accessTokenResponse.getIdToken(), IDToken.class);
        tokenVerifier.parse();

        // check for the username claim
        var idToken = tokenVerifier.getToken();
        var preferredUsername = (String) idToken.getPreferredUsername();

        // idToken.getOtherClaims().get("customcliam")

        assertThat(preferredUsername).isNotNull();
        assertThat(preferredUsername).isEqualTo(dynamicUser.getUsername());
    }

    @Test
    public void ensureCustomEventListenerLogsUserEvent() throws Exception {

        var consumer = new ToStringConsumer();
        KEYCLOAK_ENVIRONMENT.getKeycloak().followOutput(consumer);

        var tokenService = KEYCLOAK_ENVIRONMENT.getTokenService();

        // trigger user login via ROPC
        tokenService.grantToken(TEST_REALM_NAME, new Form() //
                .param("grant_type", "password") //
                .param("username", TEST_USER_USERNAME) //
                .param("password", TEST_USER_PASSWORD) //
                .param("client_id", TEST_CLIENT_CLIENT_ID) //
                .param("scope", "openid profile") //
                .asMap());

        // Allow the container log to flush
        TimeUnit.MILLISECONDS.sleep(750);

        assertThat(consumer.toUtf8String()).contains("CustomUserEvent");
    }
}
