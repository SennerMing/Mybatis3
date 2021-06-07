package club.musician.test;

import club.musician.dao.*;
import club.musician.entity.Department;
import club.musician.entity.Employee;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestHelloWorld {

    private static Logger logger = Logger.getLogger(TestHelloWorld.class);

    @Test
    public void testHelloWord() {
        /**
         * 1. 根据XML创建一个SqlSessionFactory的对象
         */
        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        /**
         * 2. 获取SqlSession实例，用来执行sql
         */
        try (SqlSession session = sqlSessionFactory.openSession()) {
            EmployeeDao employeeDao = session.getMapper(EmployeeDao.class);
//            EmployeeMapper employeeDao = session.getMapper(EmployeeMapper.class);
            Employee employee = employeeDao.selectEmployee(1);
            System.out.println(employee);
        }

    }


    /**
     * 测试增删改
     */
    @Test
    public void testCrud() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            Employee employee = new Employee(3,"SennerMing", "SennerMing@musician.club", "1");

            EmployeeDao employeeDao = sqlSession.getMapper(EmployeeDao.class);
//            employeeDao.saveEmployee(employee);

//            employee.setGender("0");
//            employeeDao.updateEmployee(employee);
            employeeDao.deleteEmployee(3);

            sqlSession.commit();
        }
    }


    @Test
    public void testEmpAndDept() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {


            EmployeeMapperPlus employeeDao = sqlSession.getMapper(EmployeeMapperPlus.class);
            Employee employee = employeeDao.getEmployeeAndDept(2);
            System.out.println(employee);
        }
    }


    @Test
    public void testEmpAndDeptStep() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            EmployeeMapperPlus employeeDao = sqlSession.getMapper(EmployeeMapperPlus.class);
            Employee employee = employeeDao.getEmployeeAndDepartmentStep(2);
            System.out.println(employee);
        }
    }


    @Test
    public void testDepartmentMultiEmployee() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            DepartmentMapper departmentMapper = sqlSession.getMapper(DepartmentMapper.class);
            Department department = departmentMapper.getDepartAndEmployee(2);
            System.out.println(department);
        }
    }

    @Test
    public void testDepartmentMultiEmployeeStep() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            DepartmentMapper departmentMapper = sqlSession.getMapper(DepartmentMapper.class);
            Department department = departmentMapper.getDepartStep(1);
            System.out.println(department);
        }
    }


    @Test
    public void testDiscriminator() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            EmployeeMapperPlus employeeMapperPlus = sqlSession.getMapper(EmployeeMapperPlus.class);
            System.out.println(employeeMapperPlus.testDiscriminator(1));
        }
    }




    @Test
    public void testDynamicSql() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            EmployeeDynamicMapper employeeDynamicMapper = sqlSession.getMapper(EmployeeDynamicMapper.class);
            Employee employee = new Employee();
//            employee.setLastName(null);
//            employee.setDepartment(null);
//            employee.setGender(null);
//            employee.setId(null);

            employee.setEmail("jerry@musician.com");

            System.out.println(employeeDynamicMapper.getEmployeeCondition(employee));
        }
    }


    @Test
    public void testDynamicSqlTrim() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            EmployeeDynamicMapper employeeDynamicMapper = sqlSession.getMapper(EmployeeDynamicMapper.class);
            Employee employee = new Employee();
//            employee.setLastName(null);
//            employee.setDepartment(null);
//            employee.setGender(null);
//            employee.setId(null);

            employee.setEmail("jerry@musician.com");

            System.out.println(employeeDynamicMapper.getEmployeeConditionTrim(employee));
        }
    }

    @Test
    public void testDynamicSqlChoose() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            EmployeeDynamicMapper employeeDynamicMapper = sqlSession.getMapper(EmployeeDynamicMapper.class);
            Employee employee = new Employee();
//            employee.setLastName(null);
//            employee.setDepartment(null);
//            employee.setGender(null);
//            employee.setId(null);

//            employee.setEmail("jerry@musician.com");
            employee.setLastName("jerry");

            System.out.println(employeeDynamicMapper.getEmployeeConditionChoose(employee));
        }
    }


    @Test
    public void testDynamicSqlSet() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            EmployeeDynamicMapper employeeDynamicMapper = sqlSession.getMapper(EmployeeDynamicMapper.class);
            Employee employee = new Employee();
//            employee.setLastName(null);
//            employee.setDepartment(null);
//            employee.setGender(null);
//            employee.setId(null);

//            employee.setEmail("jerry@musician.com");
            employee.setId(2);
            employee.setGender("1");

            employeeDynamicMapper.updateEmployeeSet(employee);
        }
    }

    @Test
    public void testDynamicSqlForeach() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            EmployeeDynamicMapper employeeDynamicMapper = sqlSession.getMapper(EmployeeDynamicMapper.class);

            List<Integer> ids = Arrays.asList(1,2);

            employeeDynamicMapper.getEmployeeConditionForeach(ids);
        }
    }

    @Test
    public void testDynamicSqlInsertForeach() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            EmployeeDynamicMapper employeeDynamicMapper = sqlSession.getMapper(EmployeeDynamicMapper.class);

            List<Employee> employeeList = new ArrayList<>();
            Employee employee1 = new Employee();
            Department department1 = new Department();
            employee1.setLastName("xiaoLi");
            employee1.setGender("0");
            employee1.setEmail("xiaoLi@musician.club");
            department1.setId(1);
            employee1.setDepartment(department1);

            Employee employee2 = new Employee();
            employee2.setLastName("xiaoZhang");
            employee2.setEmail("xiaoZhang@musician.club");
            employee2.setGender("1");
            Department department2 = new Department();
            department2.setId(0);
            employee2.setDepartment(department2);

            employeeList.add(employee1);
            employeeList.add(employee2);

            employeeDynamicMapper.addEmployeeForeach(employeeList);
            sqlSession.commit();
        }
    }



    @Test
    public void testDynamicSqlInsertInnerParameter() {

        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //这样获取的SqlSession不会自动提交数据
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            EmployeeDynamicMapper employeeDynamicMapper = sqlSession.getMapper(EmployeeDynamicMapper.class);

            Employee employee1 = new Employee();
            Department department1 = new Department();
            employee1.setLastName("xiao");
            employee1.setGender("0");
            employee1.setEmail("xiaoLi@musician.club");
            department1.setId(1);
            employee1.setDepartment(department1);


            List<Employee> employeeList = employeeDynamicMapper.testInnerParam(employee1);

            System.out.println(employeeList);
        }
    }
}
