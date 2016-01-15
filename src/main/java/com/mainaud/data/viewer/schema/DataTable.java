package com.mainaud.data.viewer.schema;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A Data table.
 */
public final class DataTable implements InFile {
    private String name;
    private DataFile file;
    private final List<DataColumn> columns = new ArrayList<>();

    private DataTable() {
    }

    public String getName() {
        return name;
    }

    @Override
    public DataFile getFile() {
        return file;
    }

    public List<DataColumn> getColumns() {
        return columns;
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
            table.columns.add(DataColumn.create(c -> {
                c.table(table);
                builder.accept(c);
            }));
            return this;
        }
    }
}
