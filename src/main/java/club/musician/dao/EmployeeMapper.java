package club.musician.dao;

import club.musician.entity.Employee;
import org.apache.ibatis.annotations.Select;

public interface EmployeeMapper {

    @Select("select * from tbl_employee where id= #{id}")
    Employee selectEmployee(Integer id);

}
