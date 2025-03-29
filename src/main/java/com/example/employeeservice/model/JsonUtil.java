package com.example.employeeservice.model;

import com.example.employeeservice.exception.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;

import java.util.concurrent.locks.ReentrantLock;

public class JsonUtil {
    private static final ObjectMapper objectMapper = createObjectMapper();
    private static final ReentrantLock mapperLock = new ReentrantLock();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    public static String toJson(Object object) {
        mapperLock.lock();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new JsonProcessingException("Failed to serialize object to JSON", e);
        } finally {
            mapperLock.unlock();
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        mapperLock.lock();
        try {
            return objectMapper.readValue(json, clazz);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new JsonProcessingException("Failed to deserialize JSON to " + clazz.getSimpleName(), e);
        } finally {
            mapperLock.unlock();
        }
    }

    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        mapperLock.lock();
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new JsonProcessingException("Failed to deserialize JSON to List<" + clazz.getSimpleName() + ">", e);
        } finally {
            mapperLock.unlock();
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        mapperLock.lock();
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new JsonProcessingException("Failed to deserialize JSON to " + typeReference.getType().getTypeName(),
                    e);
        } finally {
            mapperLock.unlock();
        }
    }

    public static boolean isValidJson(String json) {
        mapperLock.lock();
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            mapperLock.unlock();
        }
    }
}