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
        MapPropertySource source = new MapPropertySource("test", Map.ofEntries(
                java.util.Arrays.stream(propertyPairs)
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
}
