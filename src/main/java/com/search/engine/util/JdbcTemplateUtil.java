package com.search.engine.util;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yjj on 15/10/13.
 */

public class JdbcTemplateUtil {

    public static void appendUpdateSql(StringBuilder sql, String fieldName, List<Object> params, Object fieldValue) {

        if (fieldValue != null) {
            sql.append("and ").append(fieldName).append(" = ? ");
            params.add(fieldValue);
        }

    }

    public static void appendInsertSql(List<String> fieldNames, List<String> fieldValues, List<Object> params, String fieldName, Object fieldValue) {

        if (fieldValue != null) {
            fieldNames.add(fieldName);
            fieldValues.add("?");
            params.add(fieldValue);
        }
    }


    public String produceSqlFromListParam(String sql, List<Object> params) {

        for (Object obj : params) {
            sql = sql.replaceFirst("\\?", "'" + obj.toString() + "'");
        }

        return sql;
    }


    public String produceSqlFromObjectsParam(String sql, Object... params) {

        for (Object obj : params) {
            sql = sql.replaceFirst("\\?", "'" + obj.toString() + "'");
        }

        return sql;
    }


    final static Pattern pattern = Pattern.compile(":([^ ,]+)");

    public String produceSqlFromMapParam(String sql, Map<String, Object> params) {

        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {

            Object value = params.get(matcher.group(1));
            if (value == null) {
                continue;
            }

            sql = sql.replaceFirst(matcher.group(0), value.toString());
        }

        return sql;
    }

}











































