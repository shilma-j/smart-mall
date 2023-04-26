package smart.util;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import smart.entity.BaseEntity;
import smart.entity.FiledInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * database utils
 */
@Component
public class DbUtils {

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
     * @param entityClass 表的实体类
     * @param where       条件
     * @return 符合条件的行数
     */
    public static long count(Class<? extends BaseEntity> entityClass, Map<String, Object> where) {
        StringBuilder sql = new StringBuilder(String.format("SELECT COUNT(*) FROM `%s`", getTableName(entityClass)));
        if (CollectionUtils.isEmpty(where)) {
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
     * @param entityClass 表的实体类
     * @param where       条件
     * @return 删除的行数
     */
    public static long delete(Class<? extends BaseEntity> entityClass, Map<String, Object> where) {
        StringBuilder sql = new StringBuilder(String.format("DELETE FROM `%s` WHERE", getTableName(entityClass)));
        for (String key : where.keySet()) {
            sql.append(String.format(" `%s`=? AND", key));
        }
        sql.delete(sql.length() - 4, sql.length());
        return jdbc.update(sql.toString(), where.values().toArray());
    }


    /**
     * 获取符合条件的第一行
     *
     * @param entityClass 表的实体类
     * @param where       条件
     * @return row  数据行，没有返回null
     */
    public static Map<String, Object> first(Class<? extends BaseEntity> entityClass, Map<String, Object> where) {
        StringBuilder sql = new StringBuilder(String.format("SELECT * FROM `%s`", getTableName(entityClass)));
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
    public static String getTableName(Class<? extends BaseEntity> entity) {
        Table annotation = entity.getAnnotation(Table.class);
        if (annotation == null || !StringUtils.hasLength(annotation.name())) {
            throw new IllegalArgumentException("entity has not table name");
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


    /***
     * entity to map
     * @param entity  entity
     * @param fieldNames field names
     * @return Map<String, Object>
     */
    public static Map<String, Object> entityToMap(BaseEntity entity, String... fieldNames) {
        Set<String> names = new HashSet<>();
        Collections.addAll(names, fieldNames);
        Map<String, Object> row = new HashMap<>();
        Map<String, FiledInfo> fieldInfos = entity.getFieldInfos();
        if (fieldNames.length == 0) {
            fieldInfos.forEach((name, field) -> {
                var generated = field.getAnnotation(GeneratedValue.class);
                if (generated != null && generated.strategy() == GenerationType.IDENTITY) {
                    return;
                }
                if (field.getAnnotation(Transient.class) == null) {
                    row.put(DbUtils.camelCaseToUnderscoresNaming(name), field.getValue());
                }
            });
        } else {
            fieldInfos.forEach((name, field) -> {
                if (field.getAnnotation(Transient.class) == null && names.contains(name)) {
                    row.put(DbUtils.camelCaseToUnderscoresNaming(name), field.getValue());
                }
                names.remove(name);
            });
            if (names.size() > 0) {
                throw new IllegalArgumentException("illegal filed name: " + Arrays.toString(names.toArray()));
            }
        }
        return row;
    }

    /**
     * 插入行数据
     *
     * @param entityClass entity class
     * @param row         待插入的行
     */
    private static void insert(Class<? extends BaseEntity> entityClass, Map<String, Object> row) {
        if (row.size() == 0) {
            return;
        }
        var colNames = row.keySet().toArray(String[]::new);
        StringBuilder sql = new StringBuilder(getInsertHeader(getTableName(entityClass), colNames)).append("(").append("?,".repeat(row.size()));
        sql.deleteCharAt(sql.length() - 1).append(")");
        jdbc.update(sql.toString(), row.values().toArray());
    }

    /**
     * batch insert
     *
     * @param entityClass 表实体类
     * @param maps        待插入的行
     */
    public static void insertAll(Class<? extends BaseEntity> entityClass, Collection<Map<String, Object>> maps) {
        var iterator = maps.iterator();
        if (!iterator.hasNext()) {
            return;
        }
        var row = iterator.next();
        Stack<Object> params = new Stack<>();
        var colNames = row.keySet().toArray(String[]::new);
        if (colNames.length == 0) {
            return;
        }
        StringBuilder sql = new StringBuilder(getInsertHeader(getTableName(entityClass), colNames));
        do {
            sql.append("(");
            for (var colName : colNames) {
                sql.append("?,");
                params.push(row.get(colName));
            }
            sql.deleteCharAt(sql.length() - 1).append("),");
            row = iterator.hasNext() ? iterator.next() : null;
        } while (row != null);
        sql.deleteCharAt(sql.length() - 1);
        var arr = params.toArray();
        jdbc.update(sql.toString(), arr);
    }

    /**
     * batch insert
     *
     * @param entities   待插入的行
     * @param fieldNames 仅插入指定的字段，为空全部插入
     */
    public static void insertAll(Collection<? extends BaseEntity> entities, String... fieldNames) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        Collection<Map<String, Object>> rows = new Stack<>();
        Iterator<? extends BaseEntity> iterator = entities.iterator();
        var entity = iterator.next();
        var entityClass = entity.getClass();
        do {
            rows.add(entityToMap(entity, fieldNames));
            entity = iterator.hasNext() ? iterator.next() : null;
        } while (entity != null);
        insertAll(entityClass, rows);
    }

    /**
     * insert row
     *
     * @param entity record
     */
    public static void insert(BaseEntity entity) {
        insert(entity.getClass(), entityToMap(entity));
    }

    /**
     * insert row
     *
     * @param entity     record
     * @param fieldNames insert with filed names, default all
     */
    public static void insert(BaseEntity entity, String... fieldNames) {
        Set<String> names = new HashSet<>();
        Collections.addAll(names, fieldNames);
        Map<String, FiledInfo> fieldInfos = entity.getFieldInfos();
        Map<String, Object> row = new LinkedHashMap<>();

        fieldInfos.forEach((name, field) -> {
            if (field.getAnnotation(Transient.class) == null && (fieldNames.length == 0 || names.contains(name))) {
                row.put(DbUtils.camelCaseToUnderscoresNaming(name), field.getValue());
            }
            names.remove(name);
        });
        if (names.size() > 0) {
            throw new IllegalArgumentException("illegal filed name: " + Arrays.toString(names.toArray()));
        }
        insert(entity.getClass(), row);
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
     * @param entityClass 表的实体类
     * @param where       条件
     * @param row         待更新的列
     */
    public static int update(Class<? extends BaseEntity> entityClass, Map<String, Object> where, Map<String, Object> row) {
        StringBuilder sql = new StringBuilder(String.format("UPDATE `%s` SET", getTableName(entityClass)));
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
     * @param fieldNames update with filed names, default all
     * @return rows num
     */
    public static int update(BaseEntity entity, String... fieldNames) {
        AtomicReference<FiledInfo> idFiledInfo = new AtomicReference<>();
        Set<String> names = new HashSet<>();
        Collections.addAll(names, fieldNames);
        Map<String, FiledInfo> fieldInfos = entity.getFieldInfos();
        Map<String, Object> row = new LinkedHashMap<>();
        fieldInfos.forEach((name, field) -> {
            if (field.getAnnotation(Id.class) != null) {
                if (idFiledInfo.get() != null) {
                    throw new IllegalArgumentException("duplicate primary key");
                }
                idFiledInfo.set(field);
            } else if (field.getAnnotation(Transient.class) == null && (fieldNames.length == 0 || names.contains(name))) {
                row.put(DbUtils.camelCaseToUnderscoresNaming(name), field.getValue());
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
        return update(entity.getClass(), Map.of(primaryKey.getName(), primaryKey.getValue()), row);
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
        DbUtils.jdbc = jdbc;
    }
}