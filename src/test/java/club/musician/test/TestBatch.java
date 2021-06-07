package club.musician.test;

import club.musician.dao.EmployeePageMapper;
import club.musician.entity.Employee;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TestBatch {


    @Test
    public void testPageHelper() {
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        //拿到一个可以执行批量操作的sqlSession
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {

            EmployeePageMapper employeePageMapper = sqlSession.getMapper(EmployeePageMapper.class);

            for (int i = 0; i < 100; i++) {
                Employee employee = new Employee();
                employee.setLastName("SennerMing" + i);
                employee.setGender(String.valueOf(i % 2));
                employee.setEmail("SennerMing"+i+"@musician.club");
                employeePageMapper.addEmployee(employee);
            }
            sqlSession.commit();
        }
    }


}
