package com.mainaud.data.viewer.data.schema;


import com.mainaud.data.viewer.data.WithId;
import com.mainaud.data.viewer.data.WithIdBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A Data table.
 */
public final class DataTable implements WithFile, WithId {
    private UUID id;
    private String name;
    private DataFile file;
    private final List<DataColumn> columns = new ArrayList<>();

    private DataTable() {
    }

    @Override
    public UUID getId() {
        return id;
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

    public static final class Schema implements WithIdBuilder<Schema> {
        private DataTable table = new DataTable();

        @Override
        public Schema withId(UUID id) {
            table.id = requireNonNull(id);
            return this;
        }

        public Schema withName(String name) {
            table.name = requireNonNull(name);
            return this;
        }

        public Schema withFile(DataFile file) {
            table.file = requireNonNull(file);
            return this;
        }

        public Schema createColumn(Consumer<DataColumn.Schema> builder) {
            table.columns.add(DataColumn.create(c -> {
                c.withTable(table);
                builder.accept(c);
            }));
            return this;
        }
    }
}
