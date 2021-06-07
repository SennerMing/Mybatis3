package club.musician.test;

import club.musician.dao.EmployeePageMapper;
import club.musician.entity.Employee;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TestPageHelper {

    @Test
    public void testPageHelper() {
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            EmployeePageMapper employeePageMapper = sqlSession.getMapper(EmployeePageMapper.class);
            //调用拦截器，进行sql的分页改造
            Page<Object> page = PageHelper.startPage(5, 1);
            List<Employee> employeeList = employeePageMapper.getAllEmployees();
//            PageInfo<Employee> pageInfo = new PageInfo<>(employeeList);
            //传入要连续显示多少页
            PageInfo<Employee> pageInfo = new PageInfo<>(employeeList, 5);

            System.out.println("当前页码：" + page.getPageNum());
            System.out.println("总记录数：" + page.getTotal());
            System.out.println("每页个数：" + page.getPageSize());
            System.out.println("总页数：" + page.getPages());


            System.out.println("当前页码：" + pageInfo.getPageNum());
            System.out.println("总记录数：" + pageInfo.getTotal());
            System.out.println("每页个数：" + pageInfo.getPageSize());
            System.out.println("总页数：" + pageInfo.getPages());
            System.out.println("是否第一页：" + pageInfo.isIsFirstPage());
            System.out.println("是否最后一页：" + pageInfo.isIsLastPage());

            System.out.println();

            System.out.println("连续显示的页码：");
            int[] pageNum = pageInfo.getNavigatepageNums();
            for (int i : pageNum) {
                System.out.println(i);
            }



        }


    }

}
