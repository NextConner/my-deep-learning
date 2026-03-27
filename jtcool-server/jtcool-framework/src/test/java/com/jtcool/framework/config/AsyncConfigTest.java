package com.jtcool.framework.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {

    @Test
    void testAsyncExecutorConfiguration() {
        AsyncConfig config = new AsyncConfig();
        Executor executor = config.getAsyncExecutor();

        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(10, taskExecutor.getCorePoolSize());
        assertEquals(50, taskExecutor.getMaxPoolSize());
    }
}
