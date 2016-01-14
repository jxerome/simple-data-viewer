package com.mainaud.data.viewer.data;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class DataColumn implements InFile, Comparable<DataColumn>{
    public static final Comparator<DataColumn> COMPARATOR = Comparator.comparing((DataColumn c) -> c.name).thenComparing(c -> c.type).thenComparing(c -> c.table);

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

    @Override
    public int compareTo(DataColumn that) {
        return COMPARATOR.compare(this, that);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataColumn)) return false;
        DataColumn that = (DataColumn) o;
        return Objects.equals(name, that.name) &&
            type == that.type &&
            Objects.equals(table, that.table);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, table);
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
