package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;

import io.jfatura.support.GibHttpMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("FaturaClient — auth")
class ClientAuthTest {

    private FaturaClient prodClient;
    private FaturaClient testClient;
    private GibHttpMock gib;

    @BeforeEach
    void setUp() {
        gib = new GibHttpMock();
        prodClient = new FaturaClient(Environment.PROD, RestClient.builder().requestFactory(gib));
        testClient = new FaturaClient(Environment.TEST, RestClient.builder().requestFactory(gib));
    }

    // ─── getToken ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getToken")
    class GetToken {

        @Test
        @DisplayName("returns the token from the JSON response")
        void returnsToken() {
            gib.once("{\"token\":\"session-abc\"}");
            assertThat(prodClient.getToken("user123", "pass456")).isEqualTo("session-abc");
        }

        @Test
        @DisplayName("calls the assos-login endpoint")
        void callsAssosLogin() {
            gib.once("{\"token\":\"t\"}");
            prodClient.getToken("u", "p");
            assertThat(gib.call(0).uri().toString())
                    .isEqualTo(Environment.PROD.baseUrl() + "/earsiv-services/assos-login");
        }

        @Test
        @DisplayName("uses POST method")
        void usesPost() {
            gib.once("{\"token\":\"t\"}");
            prodClient.getToken("u", "p");
            assertThat(gib.call(0).method()).isEqualTo("POST");
        }

        @Test
        @DisplayName("PROD: sends assoscmd=anologin")
        void prodAssosCmd() {
            gib.once("{\"token\":\"t\"}");
            prodClient.getToken("u", "p");
            assertThat(gib.call(0).assoscmd()).isEqualTo("anologin");
        }

        @Test
        @DisplayName("TEST: sends assoscmd=login")
        void testAssosCmd() {
            gib.once("{\"token\":\"t\"}");
            testClient.getToken("u", "p");
            assertThat(gib.call(0).assoscmd()).isEqualTo("login");
        }

        @Test
        @DisplayName("sends userid in the body")
        void sendsUserid() {
            gib.once("{\"token\":\"t\"}");
            prodClient.getToken("myUser", "myPass");
            assertThat(gib.call(0).rawBody()).contains("userid=myUser");
        }

        @Test
        @DisplayName("sends sifre and sifre2 in the body")
        void sendsSifre() {
            gib.once("{\"token\":\"t\"}");
            prodClient.getToken("u", "secret123");
            assertThat(gib.call(0).rawBody()).contains("sifre=secret123");
            assertThat(gib.call(0).rawBody()).contains("sifre2=secret123");
        }

        @Test
        @DisplayName("sends rtype=json")
        void sendsRtype() {
            gib.once("{\"token\":\"t\"}");
            prodClient.getToken("u", "p");
            assertThat(gib.call(0).rawBody()).contains("rtype=json");
        }

        @Test
        @DisplayName("uses TEST base URL when env is TEST")
        void testBaseUrl() {
            gib.once("{\"token\":\"t\"}");
            testClient.getToken("u", "p");
            assertThat(gib.call(0).uri().toString())
                    .isEqualTo(Environment.TEST.baseUrl() + "/earsiv-services/assos-login");
        }

        @Test
        @DisplayName("sends content-type header as form-urlencoded")
        void formUrlencodedContentType() {
            gib.once("{\"token\":\"t\"}");
            prodClient.getToken("u", "p");
            assertThat(gib.call(0).headers().getFirst("content-type"))
                    .contains("application/x-www-form-urlencoded");
        }
    }

    // ─── logout ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("returns the redirect URL from response.data")
        void returnsRedirectUrl() {
            gib.once("{\"data\":\"https://earsivportal.efatura.gov.tr/login.jsp\"}");
            String result = prodClient.logout("session-abc").asText();
            assertThat(result).isEqualTo("https://earsivportal.efatura.gov.tr/login.jsp");
        }

        @Test
        @DisplayName("calls the assos-login endpoint")
        void callsAssosLogin() {
            gib.once("{\"data\":\"url\"}");
            prodClient.logout("tok");
            assertThat(gib.call(0).uri().toString())
                    .isEqualTo(Environment.PROD.baseUrl() + "/earsiv-services/assos-login");
        }

        @Test
        @DisplayName("PROD: sends assoscmd=anologin (same as login)")
        void prodLogoutCmdIsAnologin() {
            gib.once("{\"data\":\"url\"}");
            prodClient.logout("tok");
            assertThat(gib.call(0).assoscmd()).isEqualTo("anologin");
        }

        @Test
        @DisplayName("TEST: sends assoscmd=logout")
        void testLogoutCmd() {
            gib.once("{\"data\":\"url\"}");
            testClient.logout("tok");
            assertThat(gib.call(0).assoscmd()).isEqualTo("logout");
        }

        @Test
        @DisplayName("sends the token in the body")
        void sendsToken() {
            gib.once("{\"data\":\"url\"}");
            prodClient.logout("my-token-xyz");
            assertThat(gib.call(0).rawBody()).contains("token=my-token-xyz");
        }

        @Test
        @DisplayName("sends rtype=json")
        void sendsRtype() {
            gib.once("{\"data\":\"url\"}");
            prodClient.logout("tok");
            assertThat(gib.call(0).rawBody()).contains("rtype=json");
        }
    }
}
