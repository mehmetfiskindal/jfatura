package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;

import io.jfatura.autoconfigure.JFaturaAutoConfiguration;
import io.jfatura.autoconfigure.JFaturaProperties;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

@DisplayName("JFaturaAutoConfiguration")
class JFaturaAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
    }

    @AfterEach
    void tearDown() {
        if (context.isActive()) {
            context.close();
        }
    }

    private void refreshWith(String... propertyPairs) {
        StandardEnvironment env = new StandardEnvironment();
        MapPropertySource source = new MapPropertySource(
                "test",
                Map.ofEntries(java.util.Arrays.stream(propertyPairs)
                        .map(p -> Map.entry(p.split("=")[0], (Object) p.split("=")[1]))
                        .toArray(Map.Entry[]::new)));
        env.getPropertySources().addFirst(source);
        context.setEnvironment(env);
        context.register(JFaturaAutoConfiguration.class);
        context.refresh();
    }

    @Test
    @DisplayName("provides a FaturaClient bean defaulting to PROD")
    void providesBeanDefaultingToProd() {
        refreshWith();
        FaturaClient client = context.getBean(FaturaClient.class);
        assertThat(client.baseUrl()).isEqualTo(Environment.PROD.baseUrl());
    }

    @Test
    @DisplayName("jfatura.environment=TEST binds to the TEST portal")
    void testEnvironmentBinding() {
        refreshWith("jfatura.environment=TEST");
        FaturaClient client = context.getBean(FaturaClient.class);
        assertThat(client.baseUrl()).isEqualTo(Environment.TEST.baseUrl());
    }

    @Test
    @DisplayName("properties bean is registered")
    void propertiesRegistered() {
        refreshWith();
        assertThat(context.getBean(JFaturaProperties.class).getEnvironment()).isEqualTo(Environment.PROD);
    }

    @Test
    @DisplayName("[v0.3] resilience property'leri bağlanır ve client kurulur")
    void resiliencePropertiesBind() {
        refreshWith(
                "jfatura.environment=TEST",
                "jfatura.connect-timeout=10s",
                "jfatura.read-timeout=45s",
                "jfatura.login-retry.max-attempts=5",
                "jfatura.login-retry.delay=2s",
                "jfatura.token-cache.enabled=true",
                "jfatura.token-cache.ttl=15m");
        JFaturaProperties props = context.getBean(JFaturaProperties.class);
        assertThat(props.getConnectTimeout()).isEqualTo(java.time.Duration.ofSeconds(10));
        assertThat(props.getReadTimeout()).isEqualTo(java.time.Duration.ofSeconds(45));
        assertThat(props.getLoginRetry().getMaxAttempts()).isEqualTo(5);
        assertThat(props.getLoginRetry().getDelay()).isEqualTo(java.time.Duration.ofSeconds(2));
        assertThat(props.getTokenCache().isEnabled()).isTrue();
        assertThat(props.getTokenCache().getTtl()).isEqualTo(java.time.Duration.ofMinutes(15));

        FaturaClient client = context.getBean(FaturaClient.class);
        assertThat(client.baseUrl()).isEqualTo(Environment.TEST.baseUrl());
        assertThat(client.loginMaxAttempts()).isEqualTo(5);
    }
}
