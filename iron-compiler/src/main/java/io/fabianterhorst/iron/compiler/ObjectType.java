package io.fabianterhorst.iron.compiler;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import javax.lang.model.type.TypeMirror;

public enum ObjectType {
    BOOLEAN(Boolean.class.getName(), Boolean.class.getSimpleName(), "Boolean", "false"),
    FLOAT(Float.class.getName(), Float.class.getSimpleName(), "Float", "0f"),
    INTEGER(Integer.class.getName(), Integer.class.getSimpleName(), "Int", "0"),
    LONG(Long.class.getName(), Long.class.getSimpleName(), "Long", "0L"),
    STRING(String.class.getName(), String.class.getSimpleName(), "String", "null"),
    STRING_SET("java.util.Set<java.lang.String>", "Set<String>", "StringSet", "null"),;

    private final String mFullName;
    private final String mSimpleName;
    private final String mMethodName;
    private final String mDefaultValue;

    ObjectType(String fullName, String simpleName, String methodName, String defaultValue) {
        mFullName = fullName;
        mSimpleName = simpleName;
        mMethodName = methodName;
        mDefaultValue = defaultValue;
    }

    public String getFullName() {
        return mFullName;
    }

    public String getSimpleName() {
        return mSimpleName;
    }

    public String getMethodName() {
        return mMethodName;
    }

    public String getDefaultValue() {
        return mDefaultValue;
    }

    public boolean isCompatible(TypeMirror type) {
        return getFullName().equals(type.toString());
    }

    public static ObjectType from(TypeMirror fieldType) {
        String fullName = fieldType.toString();
        for (ObjectType objectType : values()) {
            if (objectType.getFullName().equals(fullName)) return objectType;
        }
        throw new IllegalArgumentException("Unsupported type: " + fullName);
    }

    public static boolean isAllowedType(TypeMirror fieldType) {
        String fullName = fieldType.toString();
        boolean found = false;
        for (ObjectType objectType : values()) {
            if (objectType.getFullName().equals(fullName)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public static String getAllowedTypes() {
        ArrayList<String> allowedTypes = new ArrayList<>(values().length);
        for (ObjectType objectType : values()) {
            allowedTypes.add(objectType.getFullName());
        }
        return StringUtils.join(allowedTypes, ", ");
    }
}