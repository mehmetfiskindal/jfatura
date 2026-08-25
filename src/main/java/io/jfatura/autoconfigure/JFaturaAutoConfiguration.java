package io.jfatura.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jfatura.FaturaClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * {@link FaturaClient} bean'ini otomatik yapılandırır.
 *
 * <p>Dışarıdan bir {@link RestClient.Builder} bean'i gelirse transport
 * (timeout dâhil) ona devredilir; {@code jfatura.connect-timeout} /
 * {@code jfatura.read-timeout} yalnızca varsayılan transport için geçerlidir.
 */
@AutoConfiguration
@ConditionalOnClass(FaturaClient.class)
@EnableConfigurationProperties(JFaturaProperties.class)
public class JFaturaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FaturaClient faturaClient(
            JFaturaProperties properties,
            org.springframework.beans.factory.ObjectProvider<RestClient.Builder> builderProvider,
            org.springframework.beans.factory.ObjectProvider<ObjectMapper> objectMapperProvider) {
        FaturaClient.Builder clientBuilder = FaturaClient.builder()
                .environment(properties.getEnvironment())
                .restClient(builderProvider.getIfAvailable(RestClient::builder))
                .objectMapper(objectMapperProvider.getIfAvailable(ObjectMapper::new));
        if (properties.getConnectTimeout() != null && builderProvider.getIfAvailable() == null) {
            clientBuilder.connectTimeout(properties.getConnectTimeout());
        }
        if (properties.getReadTimeout() != null && builderProvider.getIfAvailable() == null) {
            clientBuilder.readTimeout(properties.getReadTimeout());
        }
        if (properties.getLoginRetry().getMaxAttempts() > 1) {
            clientBuilder.loginRetry(
                    properties.getLoginRetry().getMaxAttempts(),
                    properties.getLoginRetry().getDelay());
        }
        if (properties.getTokenCache().isEnabled()) {
            clientBuilder.tokenCache(properties.getTokenCache().getTtl());
        }
        return clientBuilder.build();
    }
}
