package com.mainaud.data.viewer.data;


import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A Data table.
 */
public final class DataTable implements InFile, Comparable<DataTable> {
    public static final Comparator<DataTable> COMPARATOR = Comparator.comparing((DataTable t) -> t.name).thenComparing(t -> t.file);

    private String name;
    private DataFile file;
    private final SortedSet<DataColumn> columns = new ConcurrentSkipListSet<>();

    private DataTable() {
    }

    public String getName() {
        return name;
    }

    @Override
    public DataFile getFile() {
        return file;
    }

    public SortedSet<DataColumn> getColumns() {
        return columns;
    }

    @Override
    public int compareTo(DataTable that) {
        return COMPARATOR.compare(this, that);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataTable)) return false;
        DataTable that = (DataTable) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, file);
    }

    public static DataTable create(Consumer<Schema> builder) {
        Schema schema = new Schema();
        builder.accept(schema);
        DataTable table = schema.table;

        requireNonNull(table.name);
        requireNonNull(table.file);

        return table;

    }

    public static final class Schema {
        private DataTable table = new DataTable();

        public Schema name(String name) {
            table.name = requireNonNull(name);
            return this;
        }

        public Schema file(DataFile file) {
            table.file = requireNonNull(file);
            return this;
        }

        public Schema createColumn(Consumer<DataColumn.Schema> builder) {
            table.columns.add(DataColumn.create(c -> {c.table(table); builder.accept(c); }));
            return this;
        }
    }
}
