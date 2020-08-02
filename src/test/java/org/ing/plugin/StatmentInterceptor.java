package org.ing.plugin;

import com.mysql.cj.AbstractPreparedQuery;
import com.mysql.cj.PreparedQuery;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.sql.Statement;
import java.util.*;

@Intercepts({@Signature(type = StatementHandler.class,method = "batch",args = {Statement.class}),
        @Signature(type = StatementHandler.class,method = "update",args = {Statement.class}),
        @Signature(type = StatementHandler.class,method = "query",args = {Statement.class,ResultHandler.class}),
        @Signature(type = StatementHandler.class,method = "queryCursor",args = {Statement.class})
})
@Slf4j
public class StatmentInterceptor implements Interceptor{
    private final static Logger log= LoggerFactory.getLogger(StatmentInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable{
        String originalSql;
        //获取方法参数
        Object firstArg = invocation.getArgs()[0];
        if (Proxy.isProxyClass(firstArg.getClass())) {
            Statement   statement = (Statement) SystemMetaObject.forObject(firstArg).getValue("h.statement");
            MetaObject stmtMetaObj = SystemMetaObject.forObject(statement);
            //获取Statement对象（sql语法已经构建完毕）
            statement = (Statement) stmtMetaObj.getValue("stmt.statement");
            //获取sql语句
             originalSql = statement.toString();
        } else if(ClientPreparedStatement.class.isAssignableFrom(firstArg.getClass())){
            originalSql=((ClientPreparedStatement) firstArg).asSql();
          /*  PreparedQuery qeury=(PreparedQuery)((ClientPreparedStatement) firstArg).getQuery();
            originalSql = qeury.getOriginalSql();*/
        }else{
            Statement   statement = (Statement) firstArg;
            MetaObject stmtMeta = SystemMetaObject.forObject(statement);
            if(stmtMeta.hasGetter("query")){
                originalSql = (String) stmtMeta.getValue("query.originalSql");
            }else{
                //获取Statement对象（sql语法已经构建完毕）
                statement = (Statement) stmtMeta.getValue("stmt.statement");
                //获取sql语句
                originalSql = statement.toString();
            }
        }

        //将原始sql中的空白字符（\s包括换行符，制表符，空格符）替换为" "
        originalSql = originalSql.replaceAll("[\\s]+", " ");

        //只获取sql的select/update/insert/delete开头的sql
        int index = indexOfSqlStart(originalSql);
        if (index > 0) {
            originalSql = originalSql.substring(index);
        }

        // 计算执行 SQL 耗时
        long start = System.currentTimeMillis();
        Object result = invocation.proceed();
        long timing = System.currentTimeMillis()- start;

        //获取MapperStatement对象，获取到sql的详细信息
        Object realTarget = realTarget(invocation.getTarget());
        //获取metaObject对象
        MetaObject metaObject = SystemMetaObject.forObject(realTarget);
        //获取MappedStatement对象
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        StringBuilder formatSql = new StringBuilder()
                .append(" elapsed ：").append(timing)
                //获取Mapper信息和方法信息
                .append(" ms - ID：").append(ms.getId())
                .append(" - [")
                .append(originalSql)
                .append("]");
        //打印sql信息
        log.info(formatSql.toString());
        System.out.println(formatSql.toString());
        return result;
    }

    /**
     * <p>
     * 获得真正的处理对象,可能多层代理.
     * </p>
     */
    public static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return (T) target;
    }

    /**
     * 获取sql语句开头部分
     *
     * @param sql
     * @return
     */
    private int indexOfSqlStart(String sql) {
        String upperCaseSql = sql.toUpperCase();
        Set<Integer> set = new HashSet<>();
        set.add(upperCaseSql.indexOf("SELECT "));
        set.add(upperCaseSql.indexOf("UPDATE "));
        set.add(upperCaseSql.indexOf("INSERT "));
        set.add(upperCaseSql.indexOf("DELETE "));
        set.remove(-1);
        if (set.isEmpty()) {
            return -1;
        }
        List<Integer> list = new ArrayList<>(set);
        list.sort(Comparator.naturalOrder());
        return list.get(0);
    }

    @Override
    public Object plugin(Object target){
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties){

    }
}
