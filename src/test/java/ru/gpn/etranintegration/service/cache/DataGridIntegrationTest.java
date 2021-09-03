package ru.gpn.etranintegration.service.cache;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
class DataGridIntegrationTest {

    private static final Integer DATA_GRID_PORT = 11222;
    private static final String AUTH = "admin";
    private static final String CACHE_NAME = "CACHE";
    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";

    @Autowired
    private RemoteCacheManager remoteCacheManager;

    @Container
    private static final GenericContainer dataGrid = new GenericContainer(DockerImageName.parse("jboss/infinispan-server"))
            .withExposedPorts(DATA_GRID_PORT)
            .withEnv("USER", AUTH)
            .withEnv("PASS", AUTH);

    @DynamicPropertySource
    static void registerRedisProps(DynamicPropertyRegistry registry) {
        registry.add(
                "infinispan.remote.server-list",
                () -> dataGrid.getContainerIpAddress() + ":" + dataGrid.getFirstMappedPort()
        );
        registry.add(
                "infinispan.remote.auth-username",
                () -> AUTH
        );
        registry.add(
                "infinispan.remote.auth-password",
                () -> AUTH
        );
    }

    @Test
    void whenSaveResultInRedisCacheThenFindReturnsSavedResult() {
        remoteCacheManager.administration().createCache(CACHE_NAME, (String) null);
        remoteCacheManager.getCache(CACHE_NAME).put(KEY, VALUE);
        String actualValue = (String) remoteCacheManager.getCache(CACHE_NAME).get(KEY);
        Assertions.assertEquals(VALUE, actualValue);
    }

}
