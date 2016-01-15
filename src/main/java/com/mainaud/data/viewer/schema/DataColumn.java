package com.mainaud.data.viewer.schema;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class DataColumn implements InFile {
    private String name;
    private DataType type;
    private DataTable table;

    private DataColumn() {
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public DataTable getTable() {
        return table;
    }

    @Override
    public DataFile getFile() {
        return table == null ? null : table.getFile();
    }

    public static DataColumn create(Consumer<Schema> builder) {
        Schema schema = new Schema();
        builder.accept(schema);
        DataColumn column = schema.column;

        requireNonNull(column.name);
        requireNonNull(column.type);
        requireNonNull(column.table);

        return column;
    }

    public static final class Schema {
        private DataColumn column = new DataColumn();

        public Schema name(String name) {
            column.name = requireNonNull(name);
            return this;
        }

        public Schema type(DataType type) {
            column.type = requireNonNull(type);
            return this;
        }

        public Schema table(DataTable table) {
            column.table = requireNonNull(table);
            return this;
        }
    }
}
