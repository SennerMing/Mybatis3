package club.musician.dao;

import club.musician.entity.Employee;

import java.util.List;

public interface EmployeePageMapper {
    List<Employee> getAllEmployees();

    long addEmployee(Employee employee);
}
