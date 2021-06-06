package club.musician.test;

import club.musician.dao.EmployeeDao;
import club.musician.dao.EmployeeMapper;
import club.musician.entity.Employee;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

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





}
