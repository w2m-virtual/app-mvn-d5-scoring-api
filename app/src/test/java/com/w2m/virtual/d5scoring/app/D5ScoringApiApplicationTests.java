package com.w2m.virtual.d5scoring.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:19999",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.listener.auto-startup=false"
})
class D5ScoringApiApplicationTests {

    @Test
    void contextLoads() {
        // smoke test: el contexto arranca incluso sin broker accesible (el listener reintentará en background)
    }
}
