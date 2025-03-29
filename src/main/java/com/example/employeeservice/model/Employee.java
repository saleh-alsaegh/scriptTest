package com.example.employeeservice.model;

import com.example.employeeservice.exception.BusinessValidationException;
import java.time.LocalDate;
import java.time.Period;

public class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private double salary;
    private LocalDate joinDate;
    private String department;

    // Minimum and maximum constants
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;
    private static final double MIN_SALARY = 0;
    private static final double MAX_SALARY = 1_000_000;
    private static final int MIN_AGE = 18;

    // Constructors
    public Employee() {
    }

    public Employee(int id, String firstName, String lastName, LocalDate dateOfBirth,
            double salary, LocalDate joinDate, String department) {
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setDateOfBirth(dateOfBirth);
        setSalary(salary);
        setJoinDate(joinDate);
        setDepartment(department);
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public double getSalary() {
        return salary;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public String getDepartment() {
        return department;
    }

    // Setters with validation
    public void setId(int id) {
        if (id < 0) {
            throw new BusinessValidationException("ID must be a positive number");
        }
        this.id = id;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new BusinessValidationException("First name is required");
        }
        String trimmed = firstName.trim();
        if (trimmed.length() < MIN_NAME_LENGTH || trimmed.length() > MAX_NAME_LENGTH) {
            throw new BusinessValidationException(
                    String.format("First name must be between %d and %d characters",
                            MIN_NAME_LENGTH, MAX_NAME_LENGTH));
        }
        this.firstName = trimmed;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new BusinessValidationException("Last name is required");
        }
        String trimmed = lastName.trim();
        if (trimmed.length() < MIN_NAME_LENGTH || trimmed.length() > MAX_NAME_LENGTH) {
            throw new BusinessValidationException(
                    String.format("Last name must be between %d and %d characters",
                            MIN_NAME_LENGTH, MAX_NAME_LENGTH));
        }
        this.lastName = trimmed;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new BusinessValidationException("Date of birth is required");
        }
        if (dateOfBirth.isAfter(LocalDate.now().minusYears(MIN_AGE))) {
            throw new BusinessValidationException(
                    String.format("Employee must be at least %d years old", MIN_AGE));
        }
        this.dateOfBirth = dateOfBirth;
    }

    public void setSalary(double salary) {
        if (salary < MIN_SALARY) {
            throw new BusinessValidationException(
                    String.format("Salary cannot be negative (min: %.2f)", MIN_SALARY));
        }
        if (salary > MAX_SALARY) {
            throw new BusinessValidationException(
                    String.format("Salary exceeds maximum limit of %.2f", MAX_SALARY));
        }
        this.salary = Math.round(salary * 100) / 100.0; // Round to 2 decimal places
    }

    public void setJoinDate(LocalDate joinDate) {
        if (joinDate == null) {
            throw new BusinessValidationException("Join date is required");
        }
        if (joinDate.isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Join date cannot be in the future");
        }
        if (dateOfBirth != null && Period.between(dateOfBirth, joinDate).getYears() < MIN_AGE) {
            throw new BusinessValidationException(
                    String.format("Employee must be at least %d years old at join date", MIN_AGE));
        }
        this.joinDate = joinDate;
    }

    public void setDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new BusinessValidationException("Department is required");
        }
        this.department = department.trim();
    }

    // Validation method for complete object
    public void validate() {
        // All setters already validate, but this provides explicit validation point
        setId(id); // Re-validate ID
        setFirstName(firstName);
        setLastName(lastName);
        setDateOfBirth(dateOfBirth);
        setSalary(salary);
        setJoinDate(joinDate);
        setDepartment(department);
    }

    // Helper method for creating from strings (API input)
    public static Employee fromStrings(int id, String firstName, String lastName,
            String dobString, String salaryString,
            String joinDateString, String department) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setDateOfBirth(parseDate(dobString));
        employee.setSalary(parseSalary(salaryString));
        employee.setJoinDate(parseDate(joinDateString));
        employee.setDepartment(department);
        return employee;
    }

    private static LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (Exception e) {
            throw new BusinessValidationException("Invalid date format (expected yyyy-MM-dd)");
        }
    }

    private static double parseSalary(String salaryString) {
        try {
            return Double.parseDouble(salaryString);
        } catch (NumberFormatException e) {
            throw new BusinessValidationException("Invalid salary format");
        }
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", salary=" + salary +
                ", joinDate=" + joinDate +
                ", department='" + department + '\'' +
                '}';
    }
}