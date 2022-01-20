package smart.entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * base entity
 */
public abstract class BaseEntity {

    /**
     * get field infos
     *
     * @return field infos
     */
    public Map<String, FiledInfo> getFieldInfos() {
        Map<String, FiledInfo> filedInfos = new LinkedHashMap<>();
        FiledInfo filedInfo;
        Class<?> cls = this.getClass();
        String getterName;
        Method method;
        for (Field field : cls.getDeclaredFields()) {
            filedInfo = new FiledInfo();
            filedInfo.setName(field.getName());
            filedInfo.setType(field.getType());
            getterName = field.getType().equals(boolean.class) ? "is" : "get";
            getterName += Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            try {
                method = cls.getDeclaredMethod(getterName);
            } catch (NoSuchMethodException ignored) {
                continue;
            }
            try {
                filedInfo.setValue(method.invoke(this));
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                continue;
            }
            filedInfo.setAnnotations(field.getAnnotations());
            filedInfos.put(filedInfo.getName(), filedInfo);
        }

        return filedInfos;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName()).append("(");
        Map<String, FiledInfo> fieldInfos = getFieldInfos();
        fieldInfos.forEach((name, filed) -> {
            builder.append(name).append("=").append(filed.getValue()).append(",");
        });
        if (builder.charAt(builder.length() - 1) == ',') {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(")");
        return builder.toString();
    }

}
