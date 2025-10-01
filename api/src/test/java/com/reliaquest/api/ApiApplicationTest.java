package com.reliaquest.api;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiApplicationTest {

    private static final Logger log = LoggerFactory.getLogger(ApiApplicationTest.class);

    @Test
    void someTest() {
        log.info("Context loaded successfully");
    }

    // Additional test located in independent folders
    // Controller -> api/src/test/java/com/reliaquest/api/controller
    // Service -> api/src/test/java/com/reliaquest/api/service
}
