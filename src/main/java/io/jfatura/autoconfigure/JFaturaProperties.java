package io.jfatura.autoconfigure;

import io.jfatura.Environment;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code jfatura.*} yapılandırma özellikleri.
 *
 * <p>Örnek (application.yml):
 * {@snippet lang=yaml :
 * jfatura:
 *   environment: TEST
 * }
 */
@ConfigurationProperties(prefix = "jfatura")
public class JFaturaProperties {

    private Environment environment = Environment.PROD;

    /** Ayarlanırsa ortamın varsayılan base URL'ini geçersiz kılar. */
    private @Nullable String baseUrl;

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
}
