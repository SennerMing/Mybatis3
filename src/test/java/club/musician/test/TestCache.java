package club.musician.test;

import club.musician.dao.EmployeeCachedMapper;
import club.musician.dao.EmployeeMapper;
import club.musician.dao.EmployeeMapperPlus;
import club.musician.entity.Employee;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

public class TestCache {


    @Test
    public void testFirstLevelCache(){
        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        try(SqlSession sqlSession = sqlSessionFactory.openSession()){
            EmployeeMapperPlus employeeMapperPlus = sqlSession.getMapper(EmployeeMapperPlus.class);
            System.out.println(employeeMapperPlus.getEmployeeAndDept(1));
            sqlSession.clearCache();
            System.out.println(employeeMapperPlus.getEmployeeAndDept(1));
        }
    }


    @Test
    public void testSecondLevelCache(){
        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        SqlSession sqlSession1 = sqlSessionFactory.openSession();

        EmployeeCachedMapper employeeCachedMapper = sqlSession.getMapper(EmployeeCachedMapper.class);
        EmployeeCachedMapper employeeCachedMapper1 = sqlSession1.getMapper(EmployeeCachedMapper.class);


        /**
         * 查询出来的数据，都默认先放在一级缓存中，只有会话提交或者关闭后，一级缓存中的数据才会转移到二级缓存中
         */
        Employee employee = employeeCachedMapper.getEmployeeAndDept(1);
        System.out.println(employee);
        sqlSession.close();

        Employee employee1 = employeeCachedMapper1.getEmployeeAndDept(1);
        System.out.println(employee1);
        sqlSession1.close();
    }




}
