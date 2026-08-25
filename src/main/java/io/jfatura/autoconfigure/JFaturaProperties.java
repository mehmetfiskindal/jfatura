package io.jfatura.autoconfigure;

import io.jfatura.Environment;
import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code jfatura.*} yapılandırma özellikleri.
 *
 * <p>Örnek (application.yml):
 * {@snippet lang=yaml :
 * jfatura:
 *   environment: TEST
 *   connect-timeout: 10s
 *   read-timeout: 30s
 *   login-retry:
 *     max-attempts: 5
 *     delay: 3s
 *   token-cache:
 *     enabled: true
 *     ttl: 30m
 * }
 */
@ConfigurationProperties(prefix = "jfatura")
public class JFaturaProperties {

    private Environment environment = Environment.PROD;

    /** Ayarlanırsa ortamın varsayılan base URL'ini geçersiz kılar. */
    private @Nullable String baseUrl;

    private @Nullable Duration connectTimeout;
    private @Nullable Duration readTimeout;
    private final LoginRetry loginRetry = new LoginRetry();
    private final TokenCache tokenCache = new TokenCache();

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public @Nullable String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(@Nullable String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public @Nullable Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(@Nullable Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public @Nullable Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(@Nullable Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public LoginRetry getLoginRetry() {
        return loginRetry;
    }

    public TokenCache getTokenCache() {
        return tokenCache;
    }

    /** Oturum kilidi hatalarında tekrar deneme ayarları. */
    public static class LoginRetry {

        private int maxAttempts = 1;
        private Duration delay = Duration.ofSeconds(3);

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getDelay() {
            return delay;
        }

        public void setDelay(Duration delay) {
            this.delay = delay;
        }
    }

    /** Oturum token önbelleği ayarları. */
    public static class TokenCache {

        private boolean enabled = false;
        private Duration ttl = Duration.ofMinutes(30);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}
