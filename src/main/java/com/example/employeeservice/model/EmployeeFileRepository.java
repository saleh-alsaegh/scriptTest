package com.example.employeeservice.model;

import com.example.employeeservice.exception.DataAccessException;
import com.example.employeeservice.exception.EmployeeNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // Added this import
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class EmployeeFileRepository implements EmployeeRepository {
    private static final String FILE_PATH = "data/employees.json";
    private static final ReentrantLock fileLock = new ReentrantLock();
    private final ObjectMapper objectMapper;

    public EmployeeFileRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        initializeDataFile();
    }

    private void initializeDataFile() {
        fileLock.lock();
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
                Files.write(path, "[]".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to initialize employee data file", e);
        } finally {
            fileLock.unlock();
        }
    }

    @Override
    public List<Employee> findAll() throws DataAccessException {
        fileLock.lock();
        try {
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
            if (content.trim().isEmpty()) {
                return List.of();
            }
            return objectMapper.readValue(content,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Employee.class));
        } catch (IOException e) {
            throw new DataAccessException("Failed to read employees data", e);
        } finally {
            fileLock.unlock();
        }
    }

    @Override
    public Employee findById(int id) throws DataAccessException, EmployeeNotFoundException {
        try {
            return findAll().stream()
                    .filter(employee -> employee.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to find employee by id", e);
        }
    }

    @Override
    public boolean existsById(int id) throws DataAccessException {
        fileLock.lock();
        try {
            return findAll().stream()
                    .anyMatch(employee -> employee.getId() == id);
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to check employee existence", e);
        } finally {
            fileLock.unlock();
        }
    }

    @Override
    public List<Employee> findByNameContaining(String name) throws DataAccessException {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name parameter cannot be empty");
            }
            String searchTerm = name.toLowerCase();
            return findAll().stream()
                    .filter(employee -> employee.getFirstName().toLowerCase().contains(searchTerm) ||
                            employee.getLastName().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to search employees by name", e);
        }
    }

    @Override
    public List<Employee> findBySalaryRange(Double fromSalary, Double toSalary) throws DataAccessException {
        try {
            validateSalaryParameters(fromSalary, toSalary);
            return findAll().stream()
                    .filter(employee -> {
                        if (fromSalary != null && toSalary != null) {
                            return employee.getSalary() >= fromSalary && employee.getSalary() <= toSalary;
                        } else if (fromSalary != null) {
                            return employee.getSalary() >= fromSalary;
                        } else if (toSalary != null) {
                            return employee.getSalary() <= toSalary;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to filter employees by salary range", e);
        }
    }

    @Override
    public Employee save(Employee employee) throws DataAccessException {
        fileLock.lock();
        try {
            List<Employee> employees = findAll();
            employees.add(employee);
            persistAll(employees);
            return employee;
        } finally {
            fileLock.unlock();
        }
    }

    @Override
    public Employee update(Employee employee) throws DataAccessException {
        fileLock.lock();
        try {
            List<Employee> employees = findAll();
            employees = employees.stream()
                    .map(e -> e.getId() == employee.getId() ? employee : e)
                    .collect(Collectors.toList());
            persistAll(employees);
            return employee;
        } finally {
            fileLock.unlock();
        }
    }

    @Override
    public void delete(int id) throws DataAccessException, EmployeeNotFoundException {
        fileLock.lock();
        try {
            List<Employee> employees = findAll();
            if (employees.removeIf(e -> e.getId() == id)) {
                persistAll(employees);
            } else {
                throw new EmployeeNotFoundException("Employee not found with id: " + id);
            }
        } finally {
            fileLock.unlock();
        }
    }

    private void persistAll(List<Employee> employees) throws DataAccessException {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(employees);
            Files.write(Paths.get(FILE_PATH), json.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (JsonProcessingException e) {
            throw new DataAccessException("Failed to serialize employees data", e);
        } catch (IOException e) {
            throw new DataAccessException("Failed to write employees data", e);
        }
    }

    private void validateSalaryParameters(Double fromSalary, Double toSalary) {
        if (fromSalary != null && fromSalary < 0) {
            throw new IllegalArgumentException("From salary cannot be negative");
        }
        if (toSalary != null && toSalary < 0) {
            throw new IllegalArgumentException("To salary cannot be negative");
        }
        if (fromSalary != null && toSalary != null && fromSalary > toSalary) {
            throw new IllegalArgumentException("From salary cannot be greater than to salary");
        }
    }
}