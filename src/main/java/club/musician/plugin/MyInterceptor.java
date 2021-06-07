package club.musician.plugin;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.util.Properties;

//完成插件的签名：
//  目的就是为了告诉Mybatis当前插件用来拦截哪个对象的哪个方法
@Intercepts(
        {
                @Signature(type= StatementHandler.class,
                        method = "parameterize",args = java.sql.Statement.class)
        }
)
public class MyInterceptor implements Interceptor {

    /**
     * 拦截目标对象的目标方法的执行
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("MyPlugin intercept...........");
        //执行目标方法
        Object proceed = invocation.proceed();

//        返回处理后的结果
        return proceed;
    }

    /**
     * 包装目标对象的
     *      为目标对象创建一个代理对象
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        System.out.println("MyPlugin plugin...........");
        //借助plugin的wrap实现，来使用当前Interceptor创建代理对象
        Object wrap = Plugin.wrap(target, this);
        return wrap;
    }

    /**
     * setProperties
     *      将插件注册时的property属性设置进来
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {
        System.out.println("MyPlugin setProperties...........");
        System.out.println("插件配置的信息：" + properties);
    }
}
