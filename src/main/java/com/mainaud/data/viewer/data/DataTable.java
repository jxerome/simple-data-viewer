package com.mainaud.data.viewer.data;


import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A Data table.
 */
public final class DataTable {
    private String name;
    private DataFile file;
    //private SortedSet<DataColumn> columns;

    public static DataTable create(Consumer<Schema> builder) {
        Schema schema = new Schema();
        builder.accept(schema);
        return schema.table;

    }

    public static final class Schema {
        private DataTable table;

        public Schema name(String name) {
            table.name = requireNonNull(name);
            return this;
        }

        public Schema file(DataFile file) {
            table.file = requireNonNull(file);
            return this;
        }
    }
}
