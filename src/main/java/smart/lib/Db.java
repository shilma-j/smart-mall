package smart.lib;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import smart.entity.BaseEntity;
import smart.entity.FiledInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 数据库助手类
 */
@Component
public class Db {

    private static JdbcTemplate jdbc;

    /**
     * camel case to underscores naming name
     *
     * @param name camel case naming name
     * @return underscores naming name
     */
    public static String camelCaseToUnderscoresNaming(String name) {
        if (name == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(name);
        for (int i = 1; i < builder.length(); i++) {
            if (Character.isLowerCase(builder.charAt(i - 1)) && Character.isUpperCase(builder.charAt(i)) && Character.isLowerCase(builder.charAt(i + 1))) {
                builder.insert(i++, '_');
            }
        }
        return builder.toString().toLowerCase();
    }

    /**
     * commit
     */
    public static void commit() {
        jdbc.execute("COMMIT");
    }

    /**
     * 获取表行数
     *
     * @param table 表名
     * @return 行数
     */
    public static long count(String table) {
        return count(table, null);
    }

    /**
     * 获取表行数
     *
     * @param table 表名
     * @param where 条件
     * @return 符合条件的行数
     */
    public static long count(String table, Map<String, Object> where) {
        StringBuilder sql = new StringBuilder(String.format("SELECT COUNT(*) FROM `%s`", table));
        if (where == null || where.size() == 0) {
            return Helper.longValue(jdbc.queryForObject(sql.toString(), Long.class));
        }
        sql.append(" WHERE");
        for (String key : where.keySet()) {
            sql.append(String.format(" `%s`=? AND", key));
        }
        sql.delete(sql.length() - 4, sql.length());
        return Helper.longValue(jdbc.queryForObject(sql.toString(), Long.class, where.values().toArray()));
    }

    /**
     * 删除符合条件的行
     *
     * @param table 表明
     * @param where 条件
     * @return 删除的行数
     */
    public static long delete(String table, Map<String, Object> where) {
        StringBuilder sql = new StringBuilder(String.format("DELETE FROM `%s` WHERE", table));
        for (String key : where.keySet()) {
            sql.append(String.format(" `%s`=? AND", key));
        }
        sql.delete(sql.length() - 4, sql.length());
        return jdbc.update(sql.toString(), where.values().toArray());
    }


    /**
     * 获取符合条件的第一行
     *
     * @param table 表明
     * @param where 条件
     * @return row  数据行，没有返回null
     */
    public static Map<String, Object> first(String table, Map<String, Object> where) {
        StringBuilder sql = new StringBuilder(String.format("SELECT * FROM `%s`", table));
        if (where == null || where.size() == 0) {
            sql.append(" LIMIT 1");
            return jdbc.queryForMap(sql.toString());
        }
        sql.append(" WHERE");
        for (String key : where.keySet()) {
            sql.append(String.format(" `%s`=? AND", key));
        }
        sql.delete(sql.length() - 4, sql.length());
        sql.append(" LIMIT 1");
        try {
            return jdbc.queryForMap(sql.toString(), where.values().toArray());
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }

    }

    /**
     * get table name by entity
     *
     * @param entity table entity
     * @return table name
     */
    public static String getTableNameByEntity(Class<? extends BaseEntity> entity) {
        Table annotation = entity.getAnnotation(Table.class);
        if (annotation == null) {
            return null;
        }
        return annotation.name();
    }

    /**
     * 获取最近的自增id
     *
     * @return 自增id值
     */
    public static long getLastInsertId() {
        return Helper.longValue(jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class));
    }

    /**
     * 插入行数据
     *
     * @param table 表名字
     * @param row   待插入的行
     */
    public static void insert(String table, Map<String, Object> row) {
        if (row.size() == 0) {
            return;
        }
        var colNames = row.keySet().toArray(String[]::new);
        StringBuilder sql = new StringBuilder(getInsertHeader(table, colNames)).append("(").append("?,".repeat(row.size()));
        sql.deleteCharAt(sql.length() - 1).append(")");
        jdbc.update(sql.toString(), row.values().toArray());
    }

    /**
     * batch insert
     *
     * @param table 表名字
     * @param rows  待插入的行
     */
    public static void inserts(String table, Iterable<Map<String, Object>> rows) {
        var iterator = rows.iterator();
        if (!iterator.hasNext()) {
            return;
        }
        var row = iterator.next();
        Stack<Object> params = new Stack<>();
        var colNames = row.keySet().toArray(String[]::new);
        if (colNames.length == 0) {
            return;
        }
        StringBuilder sql = new StringBuilder(getInsertHeader(table, colNames));
        do {
            sql.append("(");
            for (var colName : colNames) {
                sql.append("?,");
                params.push(row.get(colName));
            }
            sql.deleteCharAt(sql.length() - 1).append("),");
            row = iterator.hasNext() ? iterator.next() : null;
        }
        while (row != null);
        sql.deleteCharAt(sql.length() - 1);
        var arr = params.toArray();
        jdbc.update(sql.toString(), arr);
    }

    /**
     * insert row
     *
     * @param entity record
     */
    public static void insert(BaseEntity entity) {
        Map<String, FiledInfo> fieldInfos = entity.getFieldInfos();
        Map<String, Object> row = new LinkedHashMap<>();

        fieldInfos.forEach((name, field) -> {
            var generated = field.getAnnotation(GeneratedValue.class);
            if (generated != null && generated.strategy() == GenerationType.IDENTITY) {
                return;
            }
            if (field.getAnnotation(Transient.class) == null) {
                row.put(Db.camelCaseToUnderscoresNaming(name), field.getValue());
            }
        });
        insert(getTableNameByEntity(entity.getClass()), row);
    }

    /**
     * insert row
     *
     * @param entity     record
     * @param filedNames insert with filed names, default all
     */
    public static void insert(BaseEntity entity, String... filedNames) {
        Set<String> names = new HashSet<>();
        Collections.addAll(names, filedNames);
        Map<String, FiledInfo> fieldInfos = entity.getFieldInfos();
        Map<String, Object> row = new LinkedHashMap<>();

        fieldInfos.forEach((name, field) -> {
            if (field.getAnnotation(Transient.class) == null && (filedNames.length == 0 || names.contains(name))) {
                row.put(Db.camelCaseToUnderscoresNaming(name), field.getValue());
            }
            names.remove(name);
        });
        if (names.size() > 0) {
            throw new IllegalArgumentException("illegal filed name: " + Arrays.toString(names.toArray()));
        }
        insert(getTableNameByEntity(entity.getClass()), row);
    }

    /**
     * rollback
     */
    public static void rollback() {
        jdbc.execute("ROLLBACK");
    }

    /**
     * underscores to camel case naming
     *
     * @param name underscores naming name
     * @return camel case naming name
     */
    public static String underscoresToCamelCaseNaming(String name) {
        if (name == null) {
            return null;
        }
        name = name.toLowerCase();
        StringBuilder builder = new StringBuilder(name);
        for (int i = 2; i < builder.length(); ) {
            if (builder.charAt(i - 1) == '_') {
                builder.replace(i - 1, i + 1, String.valueOf(builder.charAt(i)).toUpperCase());
            } else {
                i++;
            }
        }
        return builder.toString();
    }

    /**
     * 更新行数据
     *
     * @param table 表名字
     * @param where 条件
     * @param row   待更新的列
     */
    public static int update(String table, Map<String, Object> where, Map<String, Object> row) {
        StringBuilder sql = new StringBuilder(String.format("UPDATE `%s` SET", table));
        List<Object> params = new LinkedList<>();
        for (String key : row.keySet()) {
            if (row.get(key) != null) {
                sql.append(String.format(" `%s`=?,", key));
                params.add(row.get(key));
            }
        }
        sql.deleteCharAt(sql.length() - 1).append(" WHERE");
        for (String key : where.keySet()) {
            sql.append(String.format(" `%s`=? AND", key));
            params.add(where.get(key));
        }
        sql.delete(sql.length() - 4, sql.length());
        return jdbc.update(sql.toString(), params.toArray());
    }

    /**
     * update entity by primary key
     *
     * @param entity     row
     * @param filedNames update with filed names, default all
     * @return rows num
     */
    public static int update(BaseEntity entity, String... filedNames) {
        AtomicReference<FiledInfo> idFiledInfo = new AtomicReference<>();
        Set<String> names = new HashSet<>();
        Collections.addAll(names, filedNames);
        Map<String, FiledInfo> fieldInfos = entity.getFieldInfos();
        Map<String, Object> row = new LinkedHashMap<>();
        fieldInfos.forEach((name, field) -> {
            if (field.getAnnotation(Id.class) != null) {
                if (idFiledInfo.get() != null) {
                    throw new IllegalArgumentException("duplicate primary key");
                }
                idFiledInfo.set(field);
            } else if (field.getAnnotation(Transient.class) == null && (filedNames.length == 0 || names.contains(name))) {
                row.put(Db.camelCaseToUnderscoresNaming(name), field.getValue());
            }
            names.remove(name);
        });
        FiledInfo primaryKey = idFiledInfo.get();
        if (primaryKey == null) {
            throw new IllegalArgumentException("missing primary key");
        }
        if (names.size() > 0) {
            throw new IllegalArgumentException("illegal filed name: " + Arrays.toString(names.toArray()));
        }

        return update(getTableNameByEntity(entity.getClass()), Map.of(primaryKey.getName(), primaryKey.getValue()), row);
    }

    private static String getInsertHeader(String tableName, String... colNames) {
        StringBuilder sql = new StringBuilder("INSERT INTO `");
        sql.append(tableName).append("` (");
        for (var name : colNames) {
            sql.append("`").append(name).append("`,");
        }
        sql.deleteCharAt(sql.length() - 1);

        sql.append(") VALUES ");
        return sql.toString();
    }

    @Autowired
    public void setJdbc(JdbcTemplate jdbc) {
        Db.jdbc = jdbc;
    }
}
