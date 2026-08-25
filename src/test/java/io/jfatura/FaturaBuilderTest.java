package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jfatura.exception.GibAuthException;
import io.jfatura.support.GibHttpMock;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("FaturaClient.builder — timeout/retry/token-cache")
class FaturaBuilderTest {

    private GibHttpMock gib;

    @BeforeEach
    void setUp() {
        gib = new GibHttpMock();
    }

    private FaturaClient client() {
        return FaturaClient.builder()
                .environment(Environment.PROD)
                .restClient(RestClient.builder().requestFactory(gib))
                .build();
    }

    @Test
    @DisplayName("builder defaults to PROD")
    void defaultsToProd() {
        assertThat(client().baseUrl()).isEqualTo(Environment.PROD.baseUrl());
    }

    @Test
    @DisplayName("environment is applied through the builder")
    void environmentApplied() {
        FaturaClient test = FaturaClient.builder()
                .environment(Environment.TEST)
                .restClient(RestClient.builder().requestFactory(gib))
                .build();
        assertThat(test.baseUrl()).isEqualTo(Environment.TEST.baseUrl());
    }

    @Test
    @DisplayName("invalid durations are rejected")
    void rejectsInvalidDurations() {
        assertThatThrownBy(() -> FaturaClient.builder().connectTimeout(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FaturaClient.builder().readTimeout(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FaturaClient.builder().tokenCache(Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FaturaClient.builder().loginRetry(0, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── Login retry ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("retry: oturum kilidi sonrası ikinci deneme başarılı olur")
    void retriesOnSessionLock() {
        gib.once("{\"error\":\"1\",\"messages\":[\"Başka bir yerden oturum açılmış. birden fazla giriş yapmayın\"]}");
        gib.once("{\"token\":\"session-ok\"}");
        FaturaClient client = FaturaClient.builder()
                .restClient(RestClient.builder().requestFactory(gib))
                .loginRetry(2, Duration.ZERO)
                .build();

        assertThat(client.getToken("u", "p")).isEqualTo("session-ok");
        assertThat(gib.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("retry: deneme hakkı tükenirse GibAuthException fırlar")
    void retryExhaustedThrows() {
        for (int i = 0; i < 3; i++) {
            gib.once("{\"error\":\"1\",\"messages\":[{\"type\":\"E\",\"text\":\"Güvenli Çıkış yapılmadı\"}]}");
        }
        FaturaClient client = FaturaClient.builder()
                .restClient(RestClient.builder().requestFactory(gib))
                .loginRetry(3, Duration.ZERO)
                .build();

        assertThatThrownBy(() -> client.getToken("u", "p"))
                .isInstanceOf(GibAuthException.class)
                .hasMessageContaining("Güvenli Çıkış");
        assertThat(gib.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("retry: kilit olmayan hata tekrar denenmez")
    void doesNotRetryNonLockErrors() {
        gib.once("{\"error\":\"1\",\"messages\":[\"Kullanıcı kodu ya da parola hatalı\"]}");
        FaturaClient client = FaturaClient.builder()
                .restClient(RestClient.builder().requestFactory(gib))
                .loginRetry(5, Duration.ZERO)
                .build();

        assertThatThrownBy(() -> client.getToken("u", "wrong")).isInstanceOf(GibAuthException.class);
        assertThat(gib.count()).isEqualTo(1);
    }

    // ─── Token cache ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("cache: ikinci getToken HTTP çağırmaz")
    void cacheHitsWithoutSecondCall() {
        gib.once("{\"token\":\"session-abc\"}");
        FaturaClient client = FaturaClient.builder()
                .restClient(RestClient.builder().requestFactory(gib))
                .tokenCache(Duration.ofMinutes(30))
                .build();

        String first = client.getToken("u", "p");
        String second = client.getToken("u", "p");

        assertThat(first).isEqualTo("session-abc");
        assertThat(second).isEqualTo("session-abc");
        assertThat(gib.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("cache: farklı kimlik ayrı HTTP çağrısı yapar")
    void differentCredentialsBypassCache() {
        gib.once("{\"token\":\"t1\"}");
        gib.once("{\"token\":\"t2\"}");
        FaturaClient client = FaturaClient.builder()
                .restClient(RestClient.builder().requestFactory(gib))
                .tokenCache(Duration.ofMinutes(30))
                .build();

        assertThat(client.getToken("user1", "p")).isEqualTo("t1");
        assertThat(client.getToken("user2", "p")).isEqualTo("t2");
        assertThat(gib.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("cache: logout token'ı geçersiz kılar")
    void logoutInvalidatesCache() {
        gib.once("{\"token\":\"session-abc\"}");
        gib.once("{\"data\":\"bye\"}");
        gib.once("{\"token\":\"session-def\"}");
        FaturaClient client = FaturaClient.builder()
                .restClient(RestClient.builder().requestFactory(gib))
                .tokenCache(Duration.ofMinutes(30))
                .build();

        assertThat(client.getToken("u", "p")).isEqualTo("session-abc");
        client.logout("session-abc");
        assertThat(client.getToken("u", "p")).isEqualTo("session-def");
        assertThat(gib.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("cache: TTL dolunca token yenilenir (enjekte edilebilir Clock)")
    void expiredTtlRefreshesToken() throws Exception {
        MutableClock clock = new MutableClock(Instant.now());
        gib.once("{\"token\":\"eski-token\"}");
        gib.once("{\"token\":\"yeni-token\"}");
        FaturaClient client = FaturaClient.builder()
                .restClient(RestClient.builder().requestFactory(gib))
                .tokenCache(Duration.ofMinutes(10))
                .clock(clock)
                .build();

        assertThat(client.getToken("u", "p")).isEqualTo("eski-token");
        clock.advance(Duration.ofMinutes(11));
        assertThat(client.getToken("u", "p")).isEqualTo("yeni-token");
        assertThat(gib.count()).isEqualTo(2);
    }

    /** Testte kontrol edilen sahte saat. */
    static final class MutableClock extends Clock {

        private volatile Instant instant;

        MutableClock(Instant start) {
            this.instant = start;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
