package com.mainaud.data.viewer.data.schema;

import com.mainaud.data.viewer.data.WithId;
import com.mainaud.data.viewer.data.WithIdBuilder;

import java.util.UUID;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class DataColumn implements WithFile, WithId {
    private UUID id;
    private String name;
    private DataType type;
    private DataTable table;

    private DataColumn() {
    }

    @Override
    public UUID getId() {
        return id;
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

    public static final class Schema implements WithIdBuilder<Schema> {
        private DataColumn column = new DataColumn();

        @Override
        public Schema withId(UUID id) {
            column.id = requireNonNull(id);
            return this;
        }

        public Schema withName(String name) {
            column.name = requireNonNull(name);
            return this;
        }

        public Schema withType(DataType type) {
            column.type = requireNonNull(type);
            return this;
        }

        public Schema withTable(DataTable table) {
            column.table = requireNonNull(table);
            return this;
        }
    }
}
