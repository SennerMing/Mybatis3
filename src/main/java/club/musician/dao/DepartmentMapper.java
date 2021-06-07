package club.musician.dao;

import club.musician.entity.Department;

public interface DepartmentMapper {

    Department getDepartmentById(Integer id);


    Department getDepartAndEmployee(Integer id);


    Department getDepartStep(Integer id);

}
