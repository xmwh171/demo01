package com.example.demofirst.plugin;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * MyBatis数据权限插件：自动给用户查询SQL加数据权限,仅拦截user表的查询SQL
 */
@Intercepts({@Signature(
        type = StatementHandler.class, // 拦截SQL处理器
        method = "prepare",            // 拦截预编译方法
        // prepare方法的参数：Connection(连接)、Integer(超时时间)
        args = {Connection.class, Integer.class}
)})
public class DataPermissionPlugin implements Interceptor {

    // 正则匹配：精准匹配查询user表的SQL（兼容单表/多表关联/表别名）
    // 规则：SELECT ... FROM [空格/换行] user [空格/换行/WHERE/JOIN/ON等]
    private static final Pattern USER_TABLE_SELECT_PATTERN = Pattern.compile(
            "\\bSELECT\\b.*\\bFROM\\b\\s+user\\b\\s*(?=WHERE|JOIN|ON|GROUP|ORDER|LIMIT|$)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 1. 获取StatementHandler（SQL执行处理器）
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        // 2. 获取原始SQL
        String originalSql = (String) metaObject.getValue("delegate.boundSql.sql");
        // 空SQL直接放行
        if (originalSql == null || originalSql.trim().isEmpty()) {
            return invocation.proceed();
        }
        System.out.println("拦截前SQL:" + originalSql);

        // 2. 精准匹配：仅拦截user表的查询SQL
        if (USER_TABLE_SELECT_PATTERN.matcher(originalSql).find()) {
            // 3. 处理SQL，插入数据权限条件（status = 1）
            String newSql = addDataPermissionCondition(originalSql);
            // 4. 替换原始SQL
            metaObject.setValue("delegate.boundSql.sql", newSql);
            System.out.println("拦截并修改SQL：" + newSql);
        }

        // 5. 执行原方法
        return invocation.proceed();
    }

    /**
     * 给SQL添加数据权限条件（status = 1），兼容有无WHERE、有无ORDER BY的情况
     */
    private String addDataPermissionCondition(String originalSql) {
        // 统一转大写，方便判断关键字（避免大小写问题）
        String sqlUpper = originalSql.toUpperCase();
        // 定义需要插入条件的位置（ORDER BY/GROUP BY/LIMIT 等子句的起始索引）
        int orderByIndex = sqlUpper.indexOf("ORDER BY");
        int groupByIndex = sqlUpper.indexOf("GROUP BY");
        int limitIndex = sqlUpper.indexOf("LIMIT");

        // 找到第一个需要插入条件的位置（优先 ORDER BY，因为排序通常在最后）
        int insertIndex = originalSql.length(); // 默认插在最后
        if (orderByIndex > 0) {
            insertIndex = orderByIndex;
        } else if (groupByIndex > 0) {
            insertIndex = groupByIndex;
        } else if (limitIndex > 0) {
            insertIndex = limitIndex;
        }

        // 拆分SQL：条件部分 + 排序/分组/分页部分
        String sqlBefore = originalSql.substring(0, insertIndex).trim();
        String sqlAfter = originalSql.substring(insertIndex).trim();

        // 判断是否有WHERE子句，拼接条件
        String condition = "enable = 1";
        if (sqlBefore.toUpperCase().contains("WHERE")) {
            // 已有WHERE，拼接 AND 条件
            sqlBefore += " AND " + condition;
        } else {
            // 无WHERE，拼接 WHERE 条件
            sqlBefore += " WHERE " + condition;
        }

        // 拼接最终SQL
        return sqlBefore + " " + sqlAfter;
    }

    // 包装目标对象
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    // 插件参数配置（可在mybatis-config.xml中配置）
    @Override
    public void setProperties(Properties properties) {}
}
