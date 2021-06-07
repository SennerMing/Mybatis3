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

### 3.3 Select元素

```markdown
Select元素用来定义查询操作
id：唯一标识符
	-	用来引用这条语句，需要和接口的方法名一致
parameterType：参数类型
	-	可以不传，Mybatis会根据TypeHandler自动推断
resultType：返回值类型
	- 别名或者全类名，如果返回的是集合，定义集合中元素的类型。不能和resultMap同时使用
```

```markdown
我们通常会返回集合类型
一、返回List
List<Employee> getEmployees(String lastName);
xml中对应的resultType还是写的Employee

二、单条记录返回Map：key就是列名，value就是对应的值
Map<String,Object> getEmployee(Integer id);

xml中对应可以这样写
<select id="getEmloyee" resultType="map">
select * from tbl_employee where last_name = #{id}
</select>
我们之所以可以把Map直接写为map，是因为Mybatis已经帮我们把他起好了别名了

三、多条记录返回Map：key是这个记录的主键，value是这个Employee对象
Map<Integer,Employee> getEmployeesMap(String lastName);

<select id="selectEmployeesMap" resultType="Employee">
	select * from tbl_employee where last_name like #{lastName}
</select>
那Mybatis怎么知道哪个Employee对象的属性作为Key啊？
我们还需要在接口的方法明上写个注解！
@MapKey("id"|"lastName"...)//对应Map key的类型也要进行修改
Map<Integer,Employee> getEmployeesMap(String lastName);
```

#### 3.3.1 resultMap

```markdown
我们查出的列名和咱么这个JavaBean的属性名不相同，这个就比较尴尬了，这就需要我们做一个映射了
<!--
  自定义映射规则，封装JavaBean
      id:方便引用
      type:对应的JAVA Bean对象
-->
<resultMap id="EmployeeResultMap" type="club.musician.entity.Employee">
    <!-- 指定主键对应封装规则,column指定列名，property指定JavaBean属性名 -->
    <id column="id" property="id"></id>
    <!-- 定义普通列 -->
    <result column="last_name" property="lastName"></result>
    <!-- 其他不指定的列，会自动封装：我们只要写resultMap一般都会写全的 -->
</resultMap>
```

#### 3.3.2 联合查询-级联属性

```markdown
场景：查询一个员工的时候，对应的把他的部门信息也查出来
```

创建部门表

```sql
create table tbl_department(
    id int primary key AUTO_INCREMENT,
    name varchar(255)
)
```

为Employee添加department_id，新增Department的Java Bean

EmployeeMapperPlus.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="club.musician.dao.EmployeeMapperPlus">
    <!--
        自定义映射规则，封装JavaBean
            id:方便引用
            type:对应的JAVA Bean对象
     -->
    <resultMap id="EmployeeResultMap" type="club.musician.entity.Employee">
        <!-- 指定主键对应封装规则,column指定列名，property指定JavaBean属性名 -->
        <id column="id" property="id"></id>
        <!-- 定义普通列 -->
        <result column="last_name" property="lastName"></result>
        <!-- 其他不指定的列，会自动封装：我们只要写resultMap一般都会写全的 -->

        <result column="gender" property="gender"/>
        <result column="department_id" property="department.id"/>
        <result column="dept_name" property="department.name"/>
    </resultMap>
    
    <select id="getEmployeeAndDept" resultMap="EmployeeResultMap">
        select e.id id,e.last_name last_name,e.gender gender,
               e.department_id department_id,d.id dept_id,d.name dept_name
        from tbl_employee e,tbl_department d
        where e.id=d.id and e.id = #{id}
    </select>
</mapper>
```

还可以这样写

```xml
 <resultMap id="AssociationResultMap" type="club.musician.entity.Employee">
        <!-- 指定主键对应封装规则,column指定列名，property指定JavaBean属性名 -->
        <id column="id" property="id"></id>
        <!-- 定义普通列 -->
        <result column="last_name" property="lastName"></result>
        <!-- 其他不指定的列，会自动封装：我们只要写resultMap一般都会写全的 -->
        <result column="gender" property="gender"/>

        <!--
            association可以指定联合的JavaBean对象
            property：是对应我们的Employee的对象属性名
            javaType:指定这个属性对象的类型[不可省略]
         -->
        <association property="department" javaType="club.musician.entity.Department">
            <id column="dept_id" property="id"></id>
            <result column="dept_name" property="name"></result>
        </association>
</resultMap>
```

#### 3.3.3 联合查询-分步查询

创建Department对应的Mapper

对EmployeeMapperPlus进行修改

```xml
<!-- 
    级联查询的分步查询
        1、先按照员工id，查询出员工的信息
        2、根据员工信息的department_id，查出对应的部门信息
        3、把查询部门的信息，设置到Employee中的department属性值中
-->
<resultMap id="AssociationStep" type="club.musician.entity.Employee">
    <id column="id" property="id"/>
    <result column="last_name" property="lastName"/>
    <result column="email" property="email"/>
    <result column="gender" property="gender"/>
    <!--
        关联咱们的这个Department对象的封装规则,
        并且是通过select查询出来的结果，column指定将哪一列设置为select的查询参数
    -->
    <association column="department_id" property="department" select="club.musician.dao.DepartmentMapper.getDepartmentById">
    </association>
</resultMap>
    
<select id="getEmployeeAndDepartmentStep" resultMap="AssociationStep">
        select * from tbl_employee where id = #{id}
</select>
```

#### 3.3.4 延迟加载

首先再mybatis全局配置中进行懒加载的开启

```xml
<!-- 级联查询的懒加载配置 -->
<setting name="lazyLoadingEnabled" value="true"/>
<setting name="aggressiveLazyLoading" value="false"/>
```

注意：调用toString,equals,clone,hashCode默认触发懒加载的解决办法

```xml
<!--
         解决 懒加载时 打印对象toString 触发 懒加载
            lazyLoadTriggerMethods：指定哪个对象的方法触发一次延迟加载。
            默认值:equals,clone,hashCode,toString
-->
<setting name="lazyLoadTriggerMethods" value="false"/>
```

mapper.xml中可也对懒加载进行控制,fetchType:eager|lazy

```xml
<association column="department_id" property="department" fetchType="eager" select="club.musician.dao.DepartmentMapper.getDepartmentById">
</association>
```

#### 3.3.5 联合查询-一对多

在Department的JavaBean中添加List<Employee>，在查询部门的时候，将部门所有的Employee也都查询出来

```xml
<resultMap id="AssociationMulti" type="club.musician.entity.Department">
        <id column="id" property="id"></id>
        <result column="name" property="name"/>
        <!-- 开始关联咱么的List<Employee> -->
        <collection property="employeeList" ofType="club.musician.entity.Employee">
            <!-- 定义集合中对象的封装规则 -->
            <id column="empl_id" property="id"/>
            <result column="empl_gender" property="gender"/>
            <result column="lastName" property="lastName"/>
            <result column="email" property="email"/>
        </collection>
</resultMap>

<select id="getDepartAndEmployee" resultMap="AssociationMulti">
        select d.id as id,d.name as name,
               e.id as empl_id,e.gender as empl_gender,e.last_name as lastName,e.email as email
        from tbl_department d
        left join tbl_employee e
        on d.id = e.department_id
        where d.id = #{id}
</select>
```

那么这个怎么做成分步查询呢？

```xml
<!-- 分步查询 -->
    <resultMap id="DeptAssociationStep" type="club.musician.entity.Department">
        <id column="id" property="id"/>
        <result column="name" property="name"/>
        <!-- 关联的Employee -->
        <collection property="employeeList"
                    column="id"
                    fetchType="eager"
                    <!-- Employee getEmployeesByDeptId(String deptId); -->
                    select="club.musician.dao.EmployeeMapperPlus.getEmployeesByDeptId">
        </collection>
    </resultMap>

    <select id="getDepartStep" resultMap="DeptAssociationStep">
        select id,name from tbl_department where id = #{id}
    </select>
```

这个是<collection>中只传入了一个参数，那要是传多个参数该怎么办呢？

```xml
<!-- 关联的Employee -->
<collection property="employeeList"
            column="{deptId=id}"
            fetchType="eager"
            select="club.musician.dao.EmployeeMapperPlus.getEmployeesByDeptId">
</collection>
```

#### 3.3.6 discriminator

```xml
<!--
	discriminator：鉴别器，mybatis可以使用discriminator来判断某列的值，然后根据某列的值改变封装行为
 封装Employee:
	如果是女生：就把部门信息查询出来，否则就不查询
	如果是男生：就把last_name这一列的值赋给email；
-->
```

```xml
<!--
        discriminator：鉴别器，mybatis可以使用discriminator来判断某列的值，然后根据某列的值改变封装行为
     封装Employee:
        如果是女生：就把部门信息查询出来，否则就不查询
        如果是男生：就把last_name这一列的值赋给email；
    -->
    <resultMap id="DiscriminatorResultMap" type="club.musician.entity.Employee">
        <id column="id" property="id"/>
        <result column="last_name" property="lastName"/>
        <result column="email" property="email"/>
        <result column="gender" property="gender"/>
        <!--
            column:指定的列名
            javaType:列值对应的java类型
        -->
        <discriminator javaType="string" column="gender">
            <!-- 女生 -->
            <case value="1">
                <association column="department_id" property="department"
                             fetchType="eager" select="club.musician.dao.DepartmentMapper.getDepartmentById">
                </association>
            </case>
            <!-- 男生 -->
            <case value="0">
                <result column="last_name" property="gender"/>
            </case>
        </discriminator>
    </resultMap>

    <select id="testDiscriminator" resultMap="DiscriminatorResultMap">
        select * from tbl_employee where id = #{id}
    </select>
```

### 3.4  动态SQL

#### 3.4.1 if标签

```xml
<!--
        查询员工，要求，携带了哪个字段，查询条件就带上这个字段的值
                id=#{id}
         and last_name like #{lastName}
         and email = #{email}
         and gender=#{gender}
     -->
    <select id="getEmployeeCondition" resultType="club.musician.entity.Employee"
            parameterType="club.musician.entity.Employee">
        select * from tbl_employee
        where
          <!--
            使用的是OGNL表达式
           -->
        <if test="id!=null">
            id=#{id}
        </if>

        <if test="lastName!=null and lastName!=&quot;&quot; ">
            and last_name like #{lastName}
        </if>
        <if test="email != null and email.trim() != ''">
             and email = #{email}
        </if>
          <!-- ognl会进行字符串和数字进行转换的 -->
        <if test="gender==0 or gender==1">
            and gender = #{gender}
        </if>

    </select>
```

#### 3.4.2 where标签

上面写的非常有问题，就是and语句，某些条件没带，就惨了

方案一

```xml
修改where为where 1=1，不推荐使用
```

方案二

```xml
<!-- 使用where标签 -->
<select id="getEmployeeCondition" resultType="club.musician.entity.Employee"
            parameterType="club.musician.entity.Employee">
        select * from tbl_employee
        <where>
              <!--
                使用的是OGNL表达式
               -->
            <if test="id!=null">
                and id=#{id}
            </if>

            <if test="lastName!=null and lastName!=&quot;&quot; ">
                and last_name like #{lastName}
            </if>
            <if test="email != null and email.trim() != ''">
                 and email = #{email}
            </if>
              <!-- ognl会进行字符串和数字进行转换的 -->
            <if test="gender==0 or gender==1">
                and gender = #{gender}
            </if>
        </where>
    </select>
```

但是这样写还是有点风险，因为人家喜欢把and放在语句后面，where标签，只会去掉前面的and，写在后面会出问题的，就where标签不帮你去掉了

#### 3.4.3 trim标签

```xml
<select id="getEmployeeConditionTrim" resultType="club.musician.entity.Employee"
            parameterType="club.musician.entity.Employee">
        select * from tbl_employee
            <!--
                后面多出的and或者or where标签不能解决
                prefix="":前缀
                    trim标签体中是整个字符串拼串后的结果
                    prefix给拼串后的整个字符串加一个前缀

                prefixOverrides="":前缀覆盖
                    去掉整个爱富川前面多余的字符串

                 suffix="":后缀
                    suffix给拼串后的整个字符串加一个后缀

                 suffixOverrides=""后缀覆盖
                    去掉整个字符串后面多余的字符
             -->
        <trim prefix="where" suffixOverrides="and">
            <!--
              使用的是OGNL表达式
             -->
            <if test="id!=null">
                id=#{id} and
            </if>

            <if test="lastName!=null and lastName!=&quot;&quot; ">
                last_name like #{lastName} and
            </if>
            <if test="email != null and email.trim() != ''">
                email = #{email} and
            </if>
            <!-- ognl会进行字符串和数字进行转换的 -->
            <if test="gender==0 or gender==1">
                gender = #{gender} and
            </if>
        </trim>
    </select>
```

#### 3.4.4 choose标签

```markdown
分支选择:switch-case,带break
```

```xml
<!--
        分支选择:switch-case,带break
     -->
    <select id="getEmployeeConditionChoose" resultType="club.musician.entity.Employee"
            parameterType="club.musician.entity.Employee">
        select * from tbl_employee

        <where>
            <!--
              如果带了id就使用id查，，如果带了lastName就用lastName查，只会进入其中一个
             -->
            <choose>
                <when test="id != null">
                    id=#{id}
                </when>
                <when test="lastName != null">
                    last_name like #{lastName}
                </when>
                <when test="email != null">
                    email = #{email}
                </when>
                <otherwise>
                    gender = 0
                </otherwise>
            </choose>
        </where>
    </select>
```

#### 3.4.5 set标签

```xml
<!--
        根据条件判断进行更新了
        set可以换成trim<trim suffix="set" suffixOverrides=",">
     -->
    <update id="updateEmployeeSet">
        update tbl_employee
        <set>
            <if test="lastName != null and lastName.trim() !=''">
                lastName = #{lastName},
            </if>
            <if test="email != null and email.trim() != ''">
                email like #{email}
            </if>
            <if test="gender != null and gender!=''">
                gender = #{gender}
            </if>
        </set>
        where id = #{id}
    </update>
```

#### 3.4.6 foreach标签

##### 3.4.6.1 Mysql批量插入

```xml
<!--
        使用foreach标签
    -->
    <select id="getEmployeeConditionForeach" resultType="club.musician.entity.Employee">
        select * from tbl_employee where id in
        <!--
            collection:指定要遍历的集合
                list类型的参数会进行特殊处理，封装到map中，map的key叫list

            item：将当前遍历出的元素赋值给指定的变量
            #{变量名}就能去除变量的值，也就是当前遍历出的元素

            separator:分隔符

            open:遍历出所有结果拼一个开始的字符
            close:遍历出所有结果拼一个结束的字符

            index:索引，遍历list的时候是索引
                       遍历map的时候index就是key，item就是value
         -->
        <foreach collection="ids" item="item_id" separator="," open="(" close=")">
            #{item_id}
        </foreach>
    </select>
```

批量保存

```xml
<!--
        foreach 批量保存功能
        多次执行sql
        Mysql默认不支持以";"来执行多个sql，需要在连接串上加如allowMultiQueries=true这个参数才行
     -->
    <insert id="addEmployeeForeach">
        <foreach collection="employees" item="emp" separator=";">
            insert into tbl_employee(last_name,email,gender,department_id)
            values(#{emp.lastName},#{emp.email},#{emp.gender},#{emp.department.id})
        </foreach>
    </insert>
```

上面是Mysql支持的批量保存的，不支持values(),(),()这样的

##### 3.4.6.1 Oracle批量插入

```xml
 <!--
        oracle 批量保存
            方式1：
            begin
                insert into tbl_employee(last_name,email,gender,department_id)
                values(#{emp.lastName},#{emp.email},#{emp.gender},#{emp.department.id});
                insert into tbl_employee(last_name,email,gender,department_id)
                values(#{emp.lastName},#{emp.email},#{emp.gender},#{emp.department.id});
            end;
            方式2（中间表）：
            insert into tbl_employee(id,last_name,email)
                select seq_employee.nextval,lastName,email form(
                    select 'last_name_a_01' lastName,'email_a_02' email from dual
                    union
                    select 'last_name_b_01' lastName,'email_b_02' email from dual
                    ...
                )
            这样就可以进行批量的插入了
     -->
<!--    <insert id="addEmployeeForeach" databaseId="oracle">-->
<!--        <foreach collection="employees" item="emp" separator=";" open="begin" close="end;">-->
<!--            insert into tbl_employee(last_name,email,gender,department_id)-->
<!--            values(#{emp.lastName},#{emp.email},#{emp.gender},#{emp.department.id})-->
<!--        </foreach>-->
<!--    </insert>-->

    <insert id="addEmployeeForeach" databaseId="oracle">
        insert into tbl_employee(last_name,email,gender,department_id)
        select seq_employee.nextval,lastName,email form(
        <foreach collection="employees" item="emp" separator="union">
            select #{emp.lastName} lastName,#{emp.email} email from dual
        </foreach>)

    </insert>
```

#### 3.4.7 内置参数

```xml
<!--
        两个内置参数：
            不只是方法传递过来的参数可以被用来判断，取值
            mybatis默认还有两个内置参数：
              1). _parameter:代表整个参数
                    单个参数：_parameter就是这个参数
                    多个参数：多个参数会被封装成一个map ===》 _parameter就代表这个map
              2). _databaseId:如果配置了databaseIdProvider标签
                    _databaseId:就是代表当前数据库的别名
     -->

<!--    List<Employee> testInnerParam(Employee employee)-->
    <select id="testInnerParam" resultType="club.musician.entity.Employee">
        <if test="_databaseId == 'mysql'">
            select * from tbl_employee
            <if test="_parameter != null">
                <trim prefix="where" suffixOverrides="and">
                    <!--
                      使用的是OGNL表达式
                     -->
                    <if test="id!=null">
                        id=#{id} and
                    </if>

                    <if test="lastName!=null and lastName!=&quot;&quot; ">
                        last_name like #{lastName} and
                    </if>
                    <if test="email != null and email.trim() != ''">
                        email = #{email} and
                    </if>
                    <!-- ognl会进行字符串和数字进行转换的 -->
                    <if test="gender==0 or gender==1">
                        gender = #{gender} and
                    </if>
                </trim>
            </if>
        </if>
        <if test="_databaseId == 'oracle'">
            select * from tbl_employee_oracle

        </if>
    </select>
```

#### 3.4.8 bind标签

```xml
<!--
        bind参数：可以将OGNL表达式的值绑定到一个变量中，方便后来引用这个变量的值
         <bind name="" value=""/>
         1.比如我想对last_name进行模糊查询 那传入的lastName参数就得是 %xx%
         2.还可以将sql改成 '%${lastname}%'，但是这样不安全
         3.bind标签 <bind name="_lastName" value="'%'+lastName+'%'"/>
    -->
    <select id="testInnerParam" resultType="club.musician.entity.Employee">
        <if test="_databaseId == 'mysql'">
            select * from tbl_employee
            <if test="_parameter != null">

                <bind name="_lastName" value="lastName+'%'"/>
                <trim prefix="where" suffixOverrides="and">
                    <if test="id!=null">
                        id=#{id} and
                    </if>

                    <if test="lastName!=null and lastName!=&quot;&quot; ">
                        last_name like #{_lastName} and
                    </if>
                    <if test="email != null and email.trim() != ''">
                        email = #{email} and
                    </if>
                    <!-- ognl会进行字符串和数字进行转换的 -->
                    <if test="gender==0 or gender==1">
                        gender = #{gender} and
                    </if>
                </trim>
            </if>
        </if>
        <if test="_databaseId == 'oracle'">
            select * from tbl_employee_oracle

        </if>
    </select>
```

#### 3.4.9 sql标签

```xml
<!--
        sql标签，抽取可重用的sql片段，方便后面引用
        1.sql抽取：将咱们经常要查询的列名，或者插入用的列名抽取出来方便引用
        2.include引用：将已经抽取的sql引入到后面写的增删改标签中
                include还可以使用property标签自定义属性，sql中可以进行${引用key}
     -->
    <sql id="insertSql">
        <if test="_databaseId=='mysql'">
            last_name,email,gender,department_id
        </if>
        <if test="_databaseId=='oracle'">
            last_name,email,gender,department_id,oracle_field,${testColumn}
        </if>
    </sql>
```

引用sql

```xml
<!--
        foreach 批量保存功能
        多次执行sql
        Mysql默认不支持以";"来执行多个sql，需要在连接串上加如allowMultiQueries=true这个参数才行
     -->
    <insert id="addEmployeeForeach">
        <foreach collection="employees" item="emp" separator=";">
            insert into tbl_employee(
                <!-- 引用外部sql -->
                <include refid="insertSql">
                    <!-- 自定义一些属性 -->
                    <property name="testColumn" value="testValue"/>
                </include>
            )
            values(#{emp.lastName},#{emp.email},#{emp.gender},#{emp.department.id})
        </foreach>
    </insert>
```

## 4 Mybatis缓存机制

### 4.1 两级缓存

Mybatis包含一个非常强大的查询缓存特性，他可以非常方便的配置和定制。缓存可以极大的提升查询效率。

Mybatis框架中默认定义了两级缓存

**一级缓存**和**二级缓存**

- 默认情况下，只有一级缓存(SqlSession级别的缓存，也成为本地缓存)开启
- 二级缓存需要手动开启和配置，他是基于namespace级别的缓存
- 为了提高扩展性，mybatis定义了缓存接口cache。我们可以通过实现cache接口来自定义二级缓存

```markdown
在我们开发的过程当中，我们的菜单，基本上都是固定不变的，在相同的权限下，那我们再查询的时候，就没必要进行数据库的查询了。
```

```markdown
两级缓存：
	一级缓存(本地缓存)：
		与数据库同一次会话期间查询到的数据会放在本地缓存中。
		以后如果需要获取相同的数据，直接从缓存中拿，没必要再去查询数据库。
		SqlSession级别的缓存，一级缓存是一直开启的；无法关闭;SqlSession的一个Map
	二级缓存(全局缓存)：
		基于namespace(名称空间)级别的，一个namespace对应着一个二级缓存
		工作机制：
				1.一个会话，查询出一条数据，这个数据就会被放在当前会话的一级缓存中
				2.如果会话关闭，那么一级缓存中的数据会被保存到二级缓存中；新的会话再查询信息，就可以参照二级缓存
				3.sqlSession中既有EmployeeMapper查询出的Employee对象，又有DepartmentMapper查询出的Department对象，这两个对象会被放在不同的二级缓存中，因为mapper的namespace不同
```

```markdown
一级缓存的失效情况（没有使用到当前一级缓存的情况，效果就是还需要再向数据库发出查询）
	1.不在同一个SqlSession内，进行查询，还是会向数据库发出查询；
	2.同一个SqlSession，但是查询条件不同（当前一级缓存中还没有该查询条件的缓存）
	3.同一个SqlSession，但是在两次查询之间，进行了增删改的操作
	4.同一个SqlSession，手动清空缓存,sqlSession.clearCache();
```

```markdown
二级缓存如何使用
	1.默认也是开启的 <setting name="cacheEnabled" value="true">
	2.去mapper.xml中进行配置<cache>标签
	3.我们的POJO需要实现序列化的接口

注意：
	1.查询出来的数据，都默认先放在一级缓存中，只有会话提交或者关闭后，一级缓存中的数据才会转移到二级缓存中
	2.那这个cacheEnabled设置为false，关闭的是一级缓存还是二级缓存？
		首先二级缓存是会关闭的，一级缓存并没有关掉
	3.每个<select>标签都有useCache这个属性，那这个设置为false，关闭的是一级缓存还是二级缓存？
		一级缓存没有被关闭，二级缓存确认关闭
	4.在测试一级缓存的时候，只要进行增删改，那么就会清空缓存，增删改的标签其实有个属性flushCache="true"这就是他们会清空缓存的原因，那在两次查询之间，进行一次增删改任意的操作，那么会不会清空二级缓存呢？
		虽然Cache Hit Ratio，但是二级缓存中已经没有数据了
		也就是说，执行增删改之后，两级缓存数据都被清空了
	5.手动调用sqlSession.clearCache()方法，一级缓存被清除掉了，和session相关的，和二级缓存无关
```

```markdown
其他相关缓存属性
localCacheScope - 本地缓存作用域：
	SESSION：当前会话的所有数据保存在会话缓存中
	STATEMENT:可以禁用一级缓存
```

### 4.2 缓存原理

```markdown
我们又很多的SqlSession去DB中查询数据，数据一旦查询出来之后，就会对应的放在其SqlSession对应的一级缓存中

1.SqlSesssion1[Cache1]  SqlSession2[Cache2]  SqlSession3[Cache3] ...
每个SqlSession都有其对应的一级缓存区域
↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
2.SqlSession关闭或提交
↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
3.那么就出现了二级缓存，以namespace为级别的缓存区域，EmployeeMapper、DepartmentMapper等等
namespace:EmployeeMapper[Cache1]  namespace:Department[Cache2]

那新的会话，先去二级缓存中去找，是否有对应的数据，然后再去看一级缓存，都没有才去和数据库交互

Mybatis中的缓存大都是小map，但是其提供了接口，专业的是交给专业的人做
```

## 5 Mybatis运行原理

### 5.1 四层架构

首先Mybatis框架分为四层，为别为接口层、数据处理层、框架支撑层以及引导层

- 接口层
  - 接口层类别：1.数据增加接口、2.数据删除接口、3.数据查询接口、4.数据修改接口、5.配置信息维护接口
  - 接口调用方式：基于Statement ID、基于Mapper接口，上面1-4的接口类型都基于这两种调用方式
- 数据处理层(按一下顺序分为4步)
  1. 参数映射 - ParameterHandler
     - 参数映射配置
     - 参数映射解析
     - 参数类型解析
  2. SQL解析 - SqlSource
     - SQL语句配置
     - SQL语句解析
     - SQL语句动态生成
  3. SQL执行 - Executor
     - SimpleExecutor
     - BatchExecutor
     - ReuseExecutor
  4. 结果处理和映射 - ResultSetHandler
     - 结果映射配置
     - 结果类型转换
- 框架支撑层
  - SQL语句配置方式
    - 基于XML配置
    - 基于注解
  - 事务管理
  - 连接池管理
  - 缓存机制
- 引导层
  - 基于XML配置方式
  - 基于Java API配置方式

### 5.2 运行流程

Mybatis在四大对象创建的过程中，都会有插件进行介入。插件可以利用动态代理机制一层层的包装目标对象，而现实在目标对象执行目标方法之前进行拦截的效果。

Mybatis允许在已映射语句执行过程中的某一点进行拦截调用。

默认情况下，Mybatis允许使用插件来拦截的方法调用包括

- Executor(update,query,flushStatements,commit,rollback,getTransaction,close,isClosed)
- ParameterHandler(getPararmeterObejct,setParameters)
- ResultSetHandler(handleResultSets,handleOutPutParameters)
- StatementHandler(prepare,parameterize,batch,update,query)

1. 通过配置文件获取了SqlSessionFactory对象

   1. 创建配置解析器

      SqlSessionFactoryBuilder.build(resource),里面创建了一个XMLConfigBuilder的parser，这个解析器进行configuration的解析，解析properties、settings、typeAliases、enviroments、mappers等，把详细信息保存到Configuration对象中

   2. 创建Mapper解析器

      XMLMapperBuilder，解析namespace，增删改标签等

   3. 创建增删改解析器

      XMLStatementBuilder，解析insert、update、select、delete，调用addMappedStatement将每个属性的每个标签解析出来，封装成MappedStatement，每一个MappedStatement就代表一个增删改查的详细信息。KnownMappers中保存着所有mapper信息，并将整理好的Mapper信息注册到MapperRegistry中，可以通过此创建MapperProxy对象

   4. 将所有配置文件的信息解析完成，整合成了一个Configuration信息

   5. 最终创建了一个DefaultSqlSessionFactory(Configuration)返回

2. 获取SqlSession对象

   1. 创建会话

      DefaultSqlSessionFactory.openSession()，实际上调用的openSessionFromDataSource()，从全局配置中拿到defaultExecutorType:[SIMPLE|REUSE|BATCH]默认SIMPLE，这只数据源基本信息等

   2. 创建通过defaultExecutorType来创建Executor

      上面说了默认的是SIMPLE，那么就创建了一个SimpleExecutor，就是用来做增删改查的

   3. 判断是否有二级缓存

      有则创建CachingExecutor是用来包装咱们这个SimpleExecutor的

   4. interceptorChain

      再拿到所有的拦截器，重新包装Executor

   5. 最终创建一个DefaultSqlSession返回，他也是包含了Configuration和Executor

3. 获取接口的代理对象(MapperProxy)

   1. 通过DefaultSqlSession.getMapper(type)获取maper代理对象

      mapperRegistry.getMapper(type)

   2. MapperProxyFactory获得mapper的代理对象

      MapperProxyFactory创建MapperProxy是一个InvocationHandler

   3. 返回MapperProxy，包含sqlSession

4. 执行增删改查的方法

   1. MapperProxy调用invoke

      代理对象调用invoke方法，而这个invoke方法，调用的是Executor的方法

   2. Executor执行

      拿到咱么当前要实行的方法类型：增删改查类型

   3. 包装参数

      包装参数类型，封装成map等操作

   4. SqlSession方法

      DefaultSqlSession:sqlSession，传入statement和parameters调用selectOne()或者其他

   5. 获取MapStatement

      找到对应的MapStatement信息，还有参数信息，传入到Executor中进行执行，执行之前判断是否缓存生成CacheKey

   6. Executor调用执行

      判断是否符合缓存策略，如何则通过CacheKey进行缓存查找，没有缓存就调用queryFromDatabase，查出之后也会保存在本地缓存

   7. StatementHandler

      创建了一个RoutingStatementHandler，根据statementType类型进行默认PREPARED创建了一个PreparedStatementHandler对象

   8. 调用PreparedStatementHandler设置参数

   9. 调用TypeHandler给sql预编译设置参数

   10. 查出数据使用ResultSetHandler处理结果；使用TypeHandler获取value值

   11. 最终返回Dao接口定义的对象

### 5.3 查询流程总结

1. 创建代理对象

2. DefaultSqlSession

3. Executor

4. StatementHandler：处理sql语句预编译与设置参数等相关工作

   1. 创建ParameterHandler和ResultSetHandler；

      ParameterHandler设置预编译参数用的；ResultSetHandler用来处理结果

   2. TypeHandler

      DefaultResultSetHandler：进行设置参数类型处理，还能处理结果集

5. JDBC Statement:PreparedStatement进行查询



### 5.4 全流程总结

1. 根据配置文件（全局，sql映射）初始化出Configuration对象
2. 创建一个DefaultSqlSession对象，它里面包含Configuration以及Executor(根据全局配置文件中的defaultExecutorType创建出对应的Executor)
3. DefaultSqlSession.getMapper()；拿到Mapper接口对应的MapperProxy
4. MapperProxy里面有DefaultSqlSession
5. 执行增删改查方法：
   1. 调用DefaultSqlSession的增删改查(Executor)
   2. 会创建一个StatementHandler对象
      1. 也会同时创建出ParameterHandler和ResultSetHandler
   3. 调用StatementHandler预编译参数以及设置参数值
      1. 使用的是ParameterHandler给sql设置参数
   4. 调用StatementHandler的增删改查方法
   5. ResultSetHandler封装结果

注意

​	四大对象，每个对象创建的时候都有一个interceptorChan.pluginAll()



## 6 Mybatis插件

### 6.1 插件原理

Mybatis在四大对象创建的过程中，都会有插件进行介入，插件可以利用动态代理机制一层层的包装目标对象，而实现在目标对象执行目标方法之前进行拦截的效果。

Mybatis允许在已映射语句执行过程中的某一点进行拦截调用。

默认情况下，Mybatis允许使用插件来拦截的方法调用包括：

- Executor(update,query,flushStatements,commit,rollback,getTransaction,close,isClosed)
- ParameterHandler(getParameterObject,setParameters)
- ResultSetHandler(handleResultSet,handleOutputPararmters)
- StatementHandler(prepare,parametersize,batch,update,query)

```markdown
在创建四大对象的时候
1. 每个创建出来的对象不是直接返回的，而是
	interceptorChain.pluginAll(parameterHandler)
2. 获取到所有的Interceptor，这个Interceptor就是插件需要实现的接口
	调用所有Interceptor的pluginAll的方法调用，返回target包装后的对象
3. 插件机制，我们可以是用插件为目标对象创建一个代理对象
	我们的插件可以为四大对象创建出代理对象
	代理对象就可以拦截到四大对象的每一个执行
```

### 6.2 插件开发

```markdown
插件编写：
1. 编写Interceptor的实现类
2. 使用@Interceptors注解完成插件的签名
3. 将写好的插件注册到全局配置文件中
```

参考interceptor.MyInterceptor

```markdown
创建动态代理的时候，是按照插件配置顺序创建层层代理对象
执行目标方法后，按照你想顺序执行
```

### 6.3 分页插件

参考test.TestPageHelper

### 6.4 Batch执行

参考testBach

与Spring整合时

```xml
<bean id="sqlSession" class="org.mybatis.spring.SqlSessionTemplate">
	<constructor-arg name="sqlSessionFactory" ref="sqlSessionFactoryBean"></constructor-arg>
  <constructor-arg name="executorType" value="BATCH">s</constructor-arg>
</bean>
```

然后在Dao中

```java
@Autowired
private SqlSession sqlSession;
```

### 6.5 自定义类型处理器

实现TypeHandler或者继承BasetTypeHandler

Mybatis-config.xml配置

```xml
<typeHandler handler="自定义的typehandler">
	<!-- 
			还可以再处理某个字段的时候告诉Mybatis用什么类型处理器
 			保存：#{empStatus,typeHandler="xxxx"}
			查询：
				resultMap中，在result标签中指定typeHandler
	-->
</typeHandler>
```





