package com.mainaud.data.viewer.schema;

public enum DataType {
    VARIABLE,
    VALUE,
    OTHER;

    public static DataType of(String s) {
        String type = s.toLowerCase();

        if ("int".equals(type))
            return VALUE;

        if (type.startsWith("varchar"))
            return VARIABLE;

        return OTHER;
    }
}
