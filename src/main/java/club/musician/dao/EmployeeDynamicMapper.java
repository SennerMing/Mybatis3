package club.musician.dao;

import club.musician.entity.Employee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmployeeDynamicMapper {

    List<Employee> getEmployeeCondition(Employee employee);

    List<Employee> getEmployeeConditionTrim(Employee employee);

    List<Employee> getEmployeeConditionChoose(Employee employee);

    void updateEmployeeSet(Employee employee);

    List<Employee> getEmployeeConditionForeach(@Param("ids") List<Integer> ids);

    Integer addEmployeeForeach(@Param("employees") List<Employee> employeeList);

    List<Employee> testInnerParam(Employee employee);

}
