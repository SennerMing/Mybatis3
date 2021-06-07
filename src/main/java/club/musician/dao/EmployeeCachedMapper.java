package club.musician.dao;

import club.musician.entity.Employee;

public interface EmployeeCachedMapper {
    Employee getEmployeeAndDept(Integer id);
}
