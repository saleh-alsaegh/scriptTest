package com.example.employeeservice;

import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShutdownHook {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    @PreDestroy
    public void onExit() {
        logger.info("Employee Service is shutting down...");

        logger.info("Employee Service shutdown completed");
    }
}