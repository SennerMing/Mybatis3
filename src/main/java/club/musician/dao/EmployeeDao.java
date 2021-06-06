package club.musician.dao;

import club.musician.entity.Employee;
import org.apache.ibatis.annotations.Param;

public interface EmployeeDao {
    Employee selectEmployee(Integer id);

    int saveEmployee(Employee employee);

    int updateEmployee(Employee employee);

    int deleteEmployee(@Param("id") Integer id);
}
