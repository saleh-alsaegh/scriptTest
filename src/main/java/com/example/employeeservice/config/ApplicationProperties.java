package com.example.employeeservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "employee")
@Validated
public class ApplicationProperties {

    @NotBlank(message = "Data file path must be specified")
    private String dataFilePath = "data/employees.json";

    @Min(value = 1, message = "Thread pool size must be at least 1")
    @Max(value = 100, message = "Thread pool size cannot exceed 100")
    private int maxThreadPoolSize = 100;

    @Min(value = 1, message = "Core pool size must be at least 1")
    private int corePoolSize = 5;

    @Min(value = 0, message = "Queue capacity cannot be negative")
    private int queueCapacity = 100;

    @NotNull(message = "Caching enabled flag must be specified")
    private Boolean enableCaching = true;

    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Invalid thread name prefix format")
    private String threadNamePrefix = "Async-";

    public String getDataFilePath() {
        try {
            Paths.get(dataFilePath);
            return dataFilePath;
        } catch (InvalidPathException e) {
            throw new ConfigurationException("Invalid file path configuration: " + dataFilePath, e);
        }
    }

    public int getMaxThreadPoolSize() {
        if (maxThreadPoolSize < corePoolSize) {
            throw new ConfigurationException(
                    "Max thread pool size (" + maxThreadPoolSize + ") " +
                            "cannot be less than core pool size (" + corePoolSize + ")");
        }
        return maxThreadPoolSize;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public Boolean isEnableCaching() {
        return enableCaching;
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public void setDataFilePath(String dataFilePath) {
        try {
            Paths.get(dataFilePath);
            this.dataFilePath = dataFilePath;
        } catch (InvalidPathException e) {
            throw new ConfigurationException("Invalid file path: " + dataFilePath, e);
        }
    }

    public void setMaxThreadPoolSize(int maxThreadPoolSize) {
        if (maxThreadPoolSize < 1) {
            throw new ConfigurationException("Max thread pool size must be positive");
        }
        this.maxThreadPoolSize = maxThreadPoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 1) {
            throw new ConfigurationException("Core pool size must be positive");
        }
        this.corePoolSize = corePoolSize;
    }

    public void setQueueCapacity(int queueCapacity) {
        if (queueCapacity < 0) {
            throw new ConfigurationException("Queue capacity cannot be negative");
        }
        this.queueCapacity = queueCapacity;
    }

    public void setEnableCaching(Boolean enableCaching) {
        if (enableCaching == null) {
            throw new ConfigurationException("Enable caching flag cannot be null");
        }
        this.enableCaching = enableCaching;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        if (threadNamePrefix == null || threadNamePrefix.trim().isEmpty()) {
            throw new ConfigurationException("Thread name prefix cannot be empty");
        }
        this.threadNamePrefix = threadNamePrefix;
    }

    public String getResolvedDataFilePath() {
        try {
            String path = dataFilePath.startsWith("/") ? dataFilePath : "./" + dataFilePath;
            Paths.get(path);
            return path;
        } catch (InvalidPathException e) {
            throw new ConfigurationException("Invalid resolved file path", e);
        }
    }

    public void validateThreadPoolConfiguration() {
        if (corePoolSize > maxThreadPoolSize) {
            throw new ConfigurationException(
                    "Core pool size (" + corePoolSize + ") " +
                            "cannot exceed max pool size (" + maxThreadPoolSize + ")");
        }
    }

    public static class ConfigurationException extends RuntimeException {
        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}