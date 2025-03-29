package com.example.employeeservice.model;

import com.example.employeeservice.exception.DataAccessException;
import com.example.employeeservice.exception.EmployeeNotFoundException;
import java.util.List;

public interface EmployeeRepository {
    List<Employee> findAll() throws DataAccessException;

    Employee findById(int id) throws DataAccessException, EmployeeNotFoundException;

    boolean existsById(int id) throws DataAccessException;

    List<Employee> findByNameContaining(String name) throws DataAccessException;

    List<Employee> findBySalaryRange(Double fromSalary, Double toSalary) throws DataAccessException;

    Employee save(Employee employee) throws DataAccessException;

    Employee update(Employee employee) throws DataAccessException;

    void delete(int id) throws DataAccessException, EmployeeNotFoundException;
}