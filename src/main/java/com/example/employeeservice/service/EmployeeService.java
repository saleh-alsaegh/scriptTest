package com.example.employeeservice.service;

import com.example.employeeservice.exception.BusinessValidationException;
import com.example.employeeservice.exception.DataAccessException;
import com.example.employeeservice.exception.NotFoundException;
import com.example.employeeservice.exception.ServiceException;
import com.example.employeeservice.exception.EmployeeNotFoundException;
import com.example.employeeservice.model.Employee;
import com.example.employeeservice.model.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final Executor asyncExecutor;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, Executor asyncExecutor) {
        this.employeeRepository = employeeRepository;
        this.asyncExecutor = asyncExecutor;
    }

    public Employee createEmployee(Employee employee) {
        try {
            validateEmployee(employee);
            checkForExistingEmployee(employee);
            return employeeRepository.save(employee);
        } catch (BusinessValidationException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to create employee", e);
        }
    }

    public Employee getEmployeeById(int id) {
        try {
            if (id <= 0) {
                throw new BusinessValidationException("Invalid employee ID: " + id);
            }
            return employeeRepository.findById(id);
        } catch (EmployeeNotFoundException e) {
            throw new NotFoundException("Employee not found with ID: " + id, e);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to retrieve employee with ID: " + id, e);
        }
    }

    public List<Employee> getEmployees(String name, Double fromSalary, Double toSalary) {
        try {
            if (StringUtils.hasText(name)) {
                return searchEmployeesByName(name.trim());
            } else if (fromSalary != null || toSalary != null) {
                return filterEmployeesBySalary(fromSalary, toSalary);
            }
            return employeeRepository.findAll();
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to retrieve employees", e);
        }
    }

    public CompletableFuture<List<Employee>> getEmployeesAsync(String name, Double fromSalary, Double toSalary) {
        return CompletableFuture.supplyAsync(() -> getEmployees(name, fromSalary, toSalary), asyncExecutor);
    }

    public Employee updateEmployee(int id, Employee employeeUpdates) {
        try {
            Employee existingEmployee = getEmployeeById(id);
            applyUpdates(existingEmployee, employeeUpdates);
            validateEmployee(existingEmployee);
            return employeeRepository.update(existingEmployee);
        } catch (BusinessValidationException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to update employee with ID: " + id, e);
        }
    }

    public void deleteEmployee(int id) {
        try {
            employeeRepository.delete(id);
        } catch (EmployeeNotFoundException e) {
            throw new NotFoundException("Employee not found with ID: " + id, e);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to delete employee with ID: " + id, e);
        }
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        try {
            if (!StringUtils.hasText(department)) {
                throw new BusinessValidationException("Department cannot be empty");
            }
            return employeeRepository.findAll().stream()
                    .filter(e -> department.equalsIgnoreCase(e.getDepartment()))
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to retrieve employees by department", e);
        }
    }

    private void validateEmployee(Employee employee) {
        if (employee == null) {
            throw new BusinessValidationException("Employee cannot be null");
        }
        if (!StringUtils.hasText(employee.getFirstName())) {
            throw new BusinessValidationException("First name is required");
        }
        if (employee.getFirstName().length() < 2 || employee.getFirstName().length() > 50) {
            throw new BusinessValidationException("First name must be between 2-50 characters");
        }
        if (!StringUtils.hasText(employee.getLastName())) {
            throw new BusinessValidationException("Last name is required");
        }
        if (employee.getLastName().length() < 2 || employee.getLastName().length() > 50) {
            throw new BusinessValidationException("Last name must be between 2-50 characters");
        }
        if (employee.getDateOfBirth() == null) {
            throw new BusinessValidationException("Date of birth is required");
        }
        if (employee.getDateOfBirth().isAfter(LocalDate.now().minusYears(18))) {
            throw new BusinessValidationException("Employee must be at least 18 years old");
        }
        if (employee.getSalary() < 0) {
            throw new BusinessValidationException("Salary cannot be negative");
        }
        if (employee.getSalary() > 1_000_000) {
            throw new BusinessValidationException("Salary exceeds maximum limit of $1,000,000");
        }
        if (employee.getJoinDate() == null) {
            throw new BusinessValidationException("Join date is required");
        }
        if (employee.getJoinDate().isBefore(employee.getDateOfBirth().plusYears(18))) {
            throw new BusinessValidationException("Employee must be at least 18 years old at join date");
        }
        if (!StringUtils.hasText(employee.getDepartment())) {
            throw new BusinessValidationException("Department is required");
        }
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (Exception e) {
            throw new BusinessValidationException("Invalid date format (expected yyyy-MM-dd)");
        }
    }

    // Use this when receiving String dates from API
    public Employee createEmployeeFromStrings(int id, String firstName, String lastName,
            String dobString, String salaryString,
            String joinDateString, String department) {
        try {
            Employee employee = new Employee();
            employee.setId(id);
            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            employee.setDateOfBirth(parseDate(dobString));
            employee.setSalary(Double.parseDouble(salaryString));
            employee.setJoinDate(parseDate(joinDateString));
            employee.setDepartment(department);
            return employee;
        } catch (NumberFormatException e) {
            throw new BusinessValidationException("Invalid salary format");
        }
    }

    private void checkForExistingEmployee(Employee employee) {
        try {
            if (employee.getId() > 0 && employeeRepository.existsById(employee.getId())) {
                throw new BusinessValidationException("Employee with ID " + employee.getId() + " already exists");
            }
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to check for existing employee", e);
        }
    }

    private List<Employee> searchEmployeesByName(String name) {
        try {
            if (name == null || name.trim().length() < 2) {
                throw new BusinessValidationException("Search term must be at least 2 characters");
            }
            return employeeRepository.findByNameContaining(name.toLowerCase());
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to search employees by name", e);
        }
    }

    private List<Employee> filterEmployeesBySalary(Double fromSalary, Double toSalary) {
        try {
            validateSalaryRange(fromSalary, toSalary);
            return employeeRepository.findBySalaryRange(fromSalary, toSalary);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to filter employees by salary", e);
        }
    }

    private void applyUpdates(Employee target, Employee source) {
        if (source.getFirstName() != null && !source.getFirstName().isBlank()) {
            target.setFirstName(source.getFirstName().trim());
        }
        if (source.getLastName() != null && !source.getLastName().isBlank()) {
            target.setLastName(source.getLastName().trim());
        }
        if (source.getDateOfBirth() != null) {
            target.setDateOfBirth(source.getDateOfBirth());
        }
        if (source.getSalary() > 0) {
            target.setSalary(source.getSalary());
        }
        if (source.getJoinDate() != null) {
            target.setJoinDate(source.getJoinDate());
        }
        if (source.getDepartment() != null && !source.getDepartment().isBlank()) {
            target.setDepartment(source.getDepartment().trim());
        }
    }

    private void validateSalaryRange(Double fromSalary, Double toSalary) {
        if (fromSalary != null && fromSalary < 0) {
            throw new BusinessValidationException("Minimum salary cannot be negative");
        }
        if (toSalary != null && toSalary < 0) {
            throw new BusinessValidationException("Maximum salary cannot be negative");
        }
        if (fromSalary != null && toSalary != null && fromSalary > toSalary) {
            throw new BusinessValidationException("Minimum salary cannot exceed maximum salary");
        }
    }
}