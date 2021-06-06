# Mybatis 学习

## 1 Mybatis相关知识

### 1.1 回顾

```markdown
一、 小工具
JDBC --->   DBUtils(QueryRunner)  --->  JDBCTemplate

- 功能相对简单；sql语句编写在Java代码中；硬编码、高耦合的方式；
- 维护不易切实际开发需求中SQL是由变化的，频繁修改的情况多见

二、 框架
1).Hibernate和JPA:
全自动，全映射，ORM(Object Relation Mapping)框架；旨在消除Sql

Hibernate相关流程[对Programer来讲为黑盒]
JavaBean ---> [编写sql ---> 预编译 ---> 设置参数 --->执行sql ---> 封装结果] ---> DB

- 长难度复杂的SQL，对于Hibernate而言处理也不容易
- 内部自动生产的SQL，不容易做特殊优化
- 基于群映射的自动框架，大量字段的POJO进行部分映射时比较困难，导致数据库性能下降


2)Mybatis：
半自动，轻量级的持久层框架
SQL定制化，由开发人员编写，SQL与Java编码分离，完成解耦；SQL交由开发人员控制

- SQL和Java编码分开，功能边界清晰，一个专注业务、一个专注数据。
```

### 1.2 Mybatis进行下载

[点击下载](https://github.com/mybatis/mybatis-3/releases)

[官方文档](https://mybatis.org/mybatis-3/zh/getting-started.html)

### 1.3 实现HelloWorld

创建一张表

```sql
create table tbl_employee(
    id int primary key auto_increment,
    last_name varchar(255),
    gender char(1),
    email varchar(255)
)
```

新增两条测试数据

```sql
INSERT INTO musicianclub.tbl_employee (last_name, gender, email) VALUES ('tom', '0', 'tom@musician.com');

INSERT INTO musicianclub.tbl_employee (last_name, gender, email) VALUES ('jerry', '1', 'jerry@musician.com');
```

将下载来的mybatis-3.x.x.jar加入新建的Java工程，还需要配置日志信息，方便后续的调试与学习

编写创建SqlSessionFactory的代码，参考代码：TestHelloWord.java

```java
String resource = "mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
```

那么这个SqlSessionFactory是干嘛用呢？

```markdown
官方文档：既然有了 SqlSessionFactory，顾名思义，我们可以从中获得 SqlSession 的实例。SqlSession 提供了在数据库执行 SQL 命令所需的所有方法。你可以通过 SqlSession 实例来直接执行已映射的 SQL 语句。
```

哦，有了这个SqlSessionFactory可以创建SqlSession，有这个SqlSession可以执行Sql语句了，离咱们的HelloWorld很近了

```java
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
    Employee employee = employeeDao.selectEmployee(1);
    System.out.println(employee);
  }

}
```

通过上面这三行java代码，我们可以想到我们还需要创建XxxMapper.java这个类，还有其对应的XxxMapper.xml

```java
package club.musician.dao;

import club.musician.entity.Employee;

public interface EmployeeDao {
    Employee selectEmployee(Integer id);
}
```



```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="club.musician.dao.EmployeeDao">
    <select id="selectEmployee" resultType="club.musician.entity.Employee">
        select * from tbl_employee where id = #{id}
    </select>
</mapper>
```

运行结束后，发现的问题

```markdown
Employee{id=1, lastName='null', email='tom@musician.com', gender='0'}
```

请问这位同志，你的lastName去哪里了？

```markdown
一、来看看我们如今的解决方案，怎么解决这个字段名和JavaBean属性名不一样的问题呢(在不修改JavaBean的情况下)？！！！
答：那么就是改sql
select id,last_name as lastName,gender,email from tbl_employee where id = #{id}
```

### 1.4 HelloWorld小结

```markdown
开发步骤回顾：
1. 首先进行数据源的配置，Mybatis作为持久层框架，这个需求很合理吧？   ---> mybatis-config.xml

2. 通过这个配置文件，我们就能通过代码进行SqlSessionFactory的创建了，而这个SqlSessionFactory可以帮我们获取SqlSession，有了这个SqlSession我们就可以进行Sql的执行

3. 配置Mapper.xml也就是需要我们写Sql语句的那个xml文件
	1) 这个mapper需要指定namespace，根据官方示例，我们可以看出这是一个带"指定语句的参数和返回值相匹配的接口"
	2) 创建mapper.xml对应的mapper interface，方法:selectEmployee(id):Employee
	3) 在mapper中指定咱们的sql的返回类型，是一个Employee
	4）完成咱们的sql语句，对应的将返回的数据库字段名称与JavaBean中的字段名称相同
	
4. 将Mapper.xml配置进入mybatis-config.xml，让mybatis知道mapper.xml的位置

5. 通过SqlSessionFactory获得SqlSession，用SqlSession获得EmployeeDao.class获得Mapper对象，使用mapper.xml中的selectEmployee(10)查找数据库中的Employee信息
```



```markdown
1. 看代码我们是通过sqlSession.getMapper(EmployeeDao.class)就获取了一个EmployeeDao接口的对象，就能进行查询方法的调用了，这个是什么啊？Proxy代理啊，典型的JDK的代理，实现了同一个接口嘛~

2. SqlSession
	1) 从名字和用法上来看，他就是一次与数据库的会话；用完即关闭
	2) SqlSession和Coneection 一样都是非线程安全的，每次使用都应该去获取新的对象
	
3. 将接口与XML配置文件绑定，那么mybatis对自动帮我们生成一个代理对象，与数据库进行交互

4. 配置文件，还可以不用配置文件的方式进行SqlSessionFactory的创建
```



## 2 Mybatis配置文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

    <!--
    properties
        1、mybatis可以使用properties来引入外部properties的内容
            resource:引入类路径下的资源
            url:引入网络路径或者磁盘路径下的资源
        2、那引入了这个properties之后，我们就可以在下面配置数据源的时候，
        进行外部配置文件的使用了
     -->
    <properties resource="jdbc.properties"></properties>

    <!--
    settings
        来看看这个settings的配置吧
        其中有一个特别管用的配置就叫做 mapUnderscoreToCamelCase，那么这个配置有什么用呢？
        就想我们之前的Employee中有个字段叫lastName,而数据库中的字段是last_name，那么开启了这个配置
        以后，咱么就不需要再修改sql语句[last_name as lastName]了
    -->
    <settings>
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>

    <!--
        typeAliases
            我们的mapper.xml写查询语句的时候啊，resultType中经常使用到我们自己写的Java 实体类啊
            我们总是需要写那个全限定名，很费程序猿的，那设置了这个typeAliases的话就省事多了，他会给
            我们写的java entity实体类创建别名，我们只要写那个别名就行了
    -->
    <typeAliases>
        <!-- 1. 但是这样写我们还是要写全限定名啊，这个工作量不还是没少嘛？！ -->
<!--        <typeAlias type="club.musician.entity.Employee" alias="employee"/>-->

        <!-- 2. 简化的写法，为某个包下的所有类，批量的起个别名
            name:指定包名(为当前包及其子包进行批量的起别名)

            注意：别名不区分大小写

            3. 还有一种写法，在对应的实体类上面写@Alias("employee")进行别名的指定
         -->
        <package name="club.musician.entity"/>

        <!--
            还有一些是Mybatis已经帮我们起好了的别名，像是java的基本类型及其包装类，还有较为负载的Date类型等
            我们起的名字，不要和他们一样

            后面开发建议使用 【全限定名】 的方式
        -->
    </typeAliases>

    <!--
        类型处理器的作用，架起Java对象的类型与数据库类型对应映射的桥梁
            StringTypeHandler
            DoubleTypeHandler等等

        提一嘴JSR-310
        jdk1.8之前，对时间处理起来并不是很好用，计算及判断，JSR-310相关的typeHandlers需要我们手动注册
        jdk1.8之后，添加了JSR-310，方便了对时间的相关处理，mybatis-3.4之后，JSR-310相关typeHandler都是自动注册的
     -->
    <typeHandlers></typeHandlers>


    <!--
        Mybatis可以拦截到咱们的SQL的执行步骤
        主要是拦截四大对象：
            Executor(update/query/commit...)
            ParameterHandler(setParameters...)
            ResultSetHandler(handleResultSets...)
            StatementHandler(prepare/parameterize/batch...)
        暂时先混个眼熟 ==== 2021年06月05日16:04:06
     -->
<!--    <plugins>-->
<!--        <plugin interceptor=""></plugin>-->
<!--    </plugins>-->


    <!--
        mybatis可以配置多种环境
           这个id就代表着，当前数据源的全局唯一标识，我们可以进行测试环境与生产环境的切换
           default = "test"

           transactionManager：事务管理器
                type: 事务类型[JDBC-JdbcTransactionFactory|MANAGED-ManagedTransactionFactory]
                最终的解决方案，是用我们的Spring，^_^
                自定义事务管理器，实现TransactionFactory接口

           dataSource：数据源
                type: [UNPOOLED-不适用连接池|POOLED-使用连接池|JNDI-Java Naming and Directory Interface]
                分别对应[UnplloedDataSourceFactory|PooleDataSourceFactory|JndiDataSourceFactory]
                自定义数据源，实现DataSourceFactory接口
     -->

    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${jdbc.driverClass}"/>
                <property name="url" value="${jdbc.url}"/>
                <property name="username" value="${jdbc.username}"/>
                <property name="password" value="${jdbc.password}"/>
            </dataSource>
        </environment>

<!--        <environment id="test">-->
<!--            <transactionManager type="JDBC"/>-->
<!--            <dataSource type="POOLED">-->
<!--                <property name="driver" value="${jdbc.driverClass}"/>-->
<!--                <property name="url" value="${jdbc.url}"/>-->
<!--                <property name="username" value="${jdbc.username}"/>-->
<!--                <property name="password" value="${jdbc.password}"/>-->
<!--            </dataSource>-->
<!--        </environment>-->
    </environments>

    <!--
    databaseIdProvider:
        1.mybatis怎么支持多数据库厂商呢？
        只需要在我们的全局配置文件中指定，我们当前使用的数据库标识进行
            type:DB_VENDOR
            作用就是根据得到的数据库厂商的标识，mybatis就能根据这个标识，来执行不同的sql
            
            我们可以在databaseIdProvider中，为不同的数据库厂商起别名，起好别名怎么用呢？
            只需要到咱么的这个Mapper.xml中，在增删改标签上添加databaseId="mysql|oracle"就行了

            如果Mapper.xml中指定了databaseId的话，那么在指定数据源的时候：
            1.会加载对应数据库标识的sql语句
            2.会忽略id相同但未指定databaseId的sql
    -->
    <databaseIdProvider type="DB_VENDOR">
        <property name="MySQL" value="mysql"/>
        <property name="Oracle" value="oracle"/>
        <property name="Sql Server" value="sqlServer"/>
    </databaseIdProvider>


    <!--
        将我们写好的sql映射文件，注册到全局配置文件中
            resource:引用类路径下的sql映射文件
            url:引用磁盘路径下的资源 file:///var/mappers/XxxMapper.xml

        还有一个属性class:类名，直接引用这个接口
     -->
    <mappers>
        <mapper resource="mapper/EmployeeMapper.xml"/>

        <!--
            如果要像下面这样的写法，那怎么才能让mybatis知道对应的mapper.xml文件呢？
            答：1.对应的就应该将这个mapper.xml放入这个class对应的包中了
                2.接口名必须和文件名相同

            还有一种就是根本就不需要这个mapperx.ml文件了，我们基于注解的方式进行sql的语句的书写

            推荐：比较重要的DAO接口我们来写SQL映射文件
                不重要的，简单的DAO我们为了开发快速可以使用注解
         -->
<!--        <mapper class="club.musician.dao.EmployeeMapper"/>-->


        <!--
            基于包的批量注册，那这样还是需要将Mapper.xml放入该包下，并且名字要相同的
         -->
<!--        <package name="club.musician.dao"/>-->

    </mappers>

</configuration>
```

```markdown
这些标签是有顺序的喔，不能随便穿插的，这些标签的功能，以后要多多使用呢~
```

## 3 Mybatis的映射文件

Mybatis的魔法之源

```markdown
映射文件指导着Mybatis如何进行数据库增晒盖茶，有着非常重要的意义；

cache	 				- 命名空间的二级缓存配置
cache-ref 		- 其他命名空间缓存配置的引用
resultmAP 		-	自定义结果集映射
parameterMap	-	已废弃！老式风格的参数映射
sql						-	抽取可重用的语句块
insert				-	映射插入语句
update				-	映射更新语句
delete				-	映射删除语句
select				- 映射查询语句
```

增删改的编写，参考club.musician.daoEmployeeDao，及resources/mapper/UserDaoMapper.xml

### 3.1 自增问题

```markdown
原生的JDBC通过statement.getGeneratedKeys()来获取咱们新增信息的id值，mybatis中其实也是这样操作的
那么咱们在<insert>中设置useGeneratedKeys="true"，那么就开启了这个功能，那我们拿到了这个id，还需要进行指定这个mysql自增的id赋给谁，所以还需要设置keyProperty="id"

一、自增主键 -- MySql
1. 获得自增id的值
<insert id="saveEmployee" parameterType="employee" useGeneratedKeys="true" keyProperty="id">


```

#### 3.1.1 Oracle自增主键方式一

```markdown
二、自增主键 -- Oracle
1.那Oracle不像mysql支持自增，但是oracle支持sequence，可以让字段完成自动增长
1).查询所有序列： select * from user_sequences; 可以通过这条语句找到该sequence的名字,比如:seq_employee

2).通过seq_name进行序列下一个值的获取
select seq_name.nextval from dual;

3).在sql语句中使用这个值，进行id的赋值，并插入数据
insert into employee(id,last_name,email,gender) values('查到的id',"sennerming","xx@email.com","1");

2.最终的写法
<insert id="saveEmployee" parameterType="employee">
	<!-- keyProperty:查出的主键值封装给javabean的那个属性 -->
	<!-- 这个查询语句要在insert语句之前执行，所以还要设置order="Before" -->
	<selectKey keyProperty="id" order="BEFORE" resultType="Integer">
		select seq_employee.nextval from dual
	</selectKey>
	<!-- 插入自增id的员工信息 -->
	insert into employee(id,last_name,email,gender) values(#{id},#{lastName},#{email},#{gender})
</insert>

运行顺序：
	1. 先运行selectKey获得序列自增的值，将值赋给javabean的id属性
	2. 再执行插入
```



#### 3.1.2 Oracle自增主键方式二

```markdown
<insert id="saveEmployee" parameterType="employee">
	<!-- keyProperty:查出的已经插入之后的主键，值封装给javabean的那个属性 -->
	<!-- 这个查询语句要在insert语句之后执行，所以还要设置order="AFTER" -->
	<selectKey keyProperty="id" order="AFTER" resultType="Integer">
		select seq_employee.currval from dual
	</selectKey>
	<!-- 插入自增id的员工信息 -->
	insert into employee(id,last_name,email,gender) values(seq_employee.nextval,#{lastName},#{email},#{gender})
</insert>
```

### 3.2 参数处理

#### 3.2.1 单个参数

```markdown
一、单个参数，Mybatis不做特殊处理
#{参数名}：取出参数值
比如你就穿个id，也就是说
<select id="getEmployee" resultType="employee">
	select * from tbl_employee where id=${idasda}
</select>
这样的写法也是可以的，即使你在Dao接口中定义的方法是 getEmployee(Integer id):Employee
```

#### 3.2.2 多个参数

```markdown
二、多个参数
比如这样写，对应的方法 selectEmployee(Integer id,String lastName):Employee
<select id="selectEmployee" resultType="employee" databaseId="mysql">
select * from tbl_employee where id = #{id} and last_name = #{lastName}
</select>
然而这样写会报错：
org.apache.ibatis.binding.BindingException:Parameter 'id' not found.Available parameters are [1,0,param1,param2]

1). 多个参数Mybatis会做特殊处理
	多个参数的话，会被封装成一个Map，且封装是有一定的规则的
	key		:		param1、param2、param3....或者是参数的Index也可以(新版本已不支持索引了)
	value	:	传入的参数值

2). 但是上面这种param1,param2...这样都对应不过来了，看着眼花
	我们可以在Dao接口方法声明上加入注解，这样的话，我们在Mapper.xml中对应的方法中就可以根据我们指定的名字进行获取了
	EmployeeDao		:		selectEmployee(@Param("id")Integer id,@Param("lastName")String lastName)
	在mapper.xml中直接使用这个名字就行了
```

#### 3.2.3 对象参数

```markdown
我们就可以直接传入POJO就行了，直接按属性名进行获取就好了
```

#### 3.2.4 传入Map

```java
Map<String,Object> map = new HashMap<>();
map.put("id",1);
map.put("lastName","sennerming");
```

```xml
<select id="getEmpByMap" resultType="employee">
select * from tbl_employee where id=#{id} and last_name = #{lastName}
</select>
```

因为多个参数Mybatis会自动封装成Map，我们将封装好的Map传入，那直接就可以根据我们自己顶一个Key进行值的获取了。

#### 3.2.5 相关思考

```markdown
1.public Employee getEmployee(@Param("id")Integer id,String lastName);
取值： id===>#{id|param1}		last_name===>{param2}

2.public Employee getEmployee(Integer id,@Param("emp")Employee emp);
取值： id===>#{param1}  last_name===>#{param2.lastName|emp.lastName}

###特别注意：如果是Collection类型 List、Set、Array也会进行特殊处理的，也是把他们封装到Map中
key使用的是
	如果是Collection====>collection[n]，如果是List，还可以使用这个key(list)
	如果是数组就是用array;
3.public Employee getEmployee(List<Integer> ids);
取值： 第一个list[0]
```

```markdown
getEmployee(@Param("id")Integer id,@Param("lasstName")String lastName)
ParamNameResolover解析参数封装Map的
name:{0=id,1=lastName}
	1. 获取每个标了@Param注解的参数中设置的值，id、lastName；赋给name；
	2. 每一次解析了一个参数给map中保存信息：(key:参数索引；value:name的值)
			name的值：
					1).标注了param注解：注解的值
					2).没有标注param注解：
							|- 全局配置:userActualParamName(jdk1.8之后);name等于参数名
							|- name=map.size()；相当于当前元素的索引
				
```

#### 3.2.6 #与$的区别

```markdown
使用${}取出来的值，直接拼入SQL中；存在安全问题
使用#{}取出来的值，以预编译的形式，PrepareStatement的形式，将参数设置到SQL语句中
```

```markdown
大多数情况下取参数的值，我们都使用#{}
在某些情况下，比如分表操作:
财务表，2016_salary、2017_salary,原生JDBC不支持占位符的地方，我们就可以使用${}进行取值
select * from ${year}_salary where xxxx;
select * from tbl_employee order by ${f_name} ${asc|desc}
```

```markdown
#{}取值的时候，用法更加丰富
可以指定很多参数：javaType、jdbcType、mode(存储过程)、numericScale、resultMap、typeHandler、jdbcTypeName、expression
```

#### 3.2.7 对NULL值的处理

```markdown
Mppaer.xml中：
jdbcType：通常需要在某种特定条件下被设置

在我们的数据为null的时候，有些数据库可能不能识别mybatis对null的默认处理，比如Oralce
因为mybatis对所有的null都映射为JDBC OTHER类型，Oracle不能正确处理；MySQL可以兼容处理

这样处理的话，oracle就会正确的处理这个NULL值：
insert into tbl_employee(id,last_name,gender,email) values(#{id},#{lastName},#{email,JdbcType=NULL},#{gender})
```

```xml
<!-- mybatis-config.xml中： -->
<settings>
	<setting name="mapUnderscoreToCamelCase" value="true"></setting>
  <setting name="jdbcTypeForNumm" value="NULL"></setting>
</settings>
```







