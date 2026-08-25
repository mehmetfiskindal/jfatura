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
 */
@AutoConfiguration
@ConditionalOnClass(FaturaClient.class)
@EnableConfigurationProperties(JFaturaProperties.class)
public class JFaturaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FaturaClient faturaClient(JFaturaProperties properties,
            org.springframework.beans.factory.ObjectProvider<RestClient.Builder> builderProvider,
            org.springframework.beans.factory.ObjectProvider<ObjectMapper> objectMapperProvider) {
        RestClient.Builder builder = builderProvider.getIfAvailable(RestClient::builder);
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        return new FaturaClient(properties.getEnvironment(), builder, objectMapper);
    }
}
