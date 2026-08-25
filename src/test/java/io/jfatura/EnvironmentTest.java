package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;

import io.jfatura.support.GibHttpMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FaturaClient — environment & constructor")
class EnvironmentTest {

    private GibHttpMock gib;

    @BeforeEach
    void setUp() {
        gib = new GibHttpMock();
    }

    private FaturaClient client(Environment env) {
        return new FaturaClient(env, org.springframework.web.client.RestClient.builder().requestFactory(gib));
    }

    @Test
    @DisplayName("defaults to PROD when no argument given")
    void defaultsToProd() {
        assertThat(new FaturaClient().environment().baseUrl()).isEqualTo(Environment.PROD.baseUrl());
    }

    @Test
    @DisplayName("accepts PROD explicitly")
    void acceptsProdExplicitly() {
        assertThat(client(Environment.PROD).baseUrl()).isEqualTo(Environment.PROD.baseUrl());
    }

    @Test
    @DisplayName("accepts TEST and uses test base URL")
    void acceptsTest() {
        assertThat(client(Environment.TEST).baseUrl()).isEqualTo(Environment.TEST.baseUrl());
    }

    @Test
    @DisplayName("PROD and TEST base URLs are different")
    void urlsDiffer() {
        assertThat(Environment.PROD.baseUrl()).isNotEqualTo(Environment.TEST.baseUrl());
    }

    @Test
    @DisplayName("sets PROD loginCmd to 'anologin'")
    void prodLoginCmd() {
        assertThat(client(Environment.PROD).loginCmd()).isEqualTo("anologin");
    }

    @Test
    @DisplayName("sets TEST loginCmd to 'login'")
    void testLoginCmd() {
        assertThat(client(Environment.TEST).loginCmd()).isEqualTo("login");
    }

    @Test
    @DisplayName("sets PROD logoutCmd to 'anologin'")
    void prodLogoutCmd() {
        assertThat(client(Environment.PROD).logoutCmd()).isEqualTo("anologin");
    }

    @Test
    @DisplayName("sets TEST logoutCmd to 'logout'")
    void testLogoutCmd() {
        assertThat(client(Environment.TEST).logoutCmd()).isEqualTo("logout");
    }

    @Nested
    @DisplayName("FaturaClient.create factory")
    class CreateFactory {

        @Test
        @DisplayName("returns a FaturaClient instance")
        void returnsClient() {
            assertThat(FaturaClient.create()).isInstanceOf(FaturaClient.class);
        }

        @Test
        @DisplayName("defaults to PROD")
        void defaultsToProd() {
            assertThat(FaturaClient.create().environment()).isEqualTo(Environment.PROD);
        }

        @Test
        @DisplayName("passes env argument through")
        void passesEnvThrough() {
            assertThat(FaturaClient.create(Environment.TEST).baseUrl()).isEqualTo(Environment.TEST.baseUrl());
        }

        @Test
        @DisplayName("two clients with different envs are independent")
        void independentClients() {
            FaturaClient prod = FaturaClient.create(Environment.PROD);
            FaturaClient test = FaturaClient.create(Environment.TEST);
            assertThat(prod.baseUrl()).isNotEqualTo(test.baseUrl());
            assertThat(prod.loginCmd()).isNotEqualTo(test.loginCmd());
        }
    }
}
