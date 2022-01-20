package smart.entity;

import java.lang.annotation.Annotation;

public final class FiledInfo {
    private Annotation[] annotations;
    // is primary key
    private boolean primaryKey = false;
    private String name;
    private Object value;
    private Class<?> type;

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for (var annotation : annotations) {
            var cls = annotation.annotationType();
            if (annotation.annotationType().equals(annotationClass)) {
                return (T) annotation;
            }
        }
        return null;
    }
    public Annotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[] annotations) {
        this.annotations = annotations;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }
}
