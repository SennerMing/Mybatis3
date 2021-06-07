package club.musician.dao;

import club.musician.entity.Employee;

public interface EmployeeMapperPlus{
    Employee getEmployeeAndDept(Integer id);

    Employee getEmployeeAndDepartmentStep(Integer id);

    Employee getEmployeesByDeptId(String deptId);

    Employee testDiscriminator(Integer id);

}
