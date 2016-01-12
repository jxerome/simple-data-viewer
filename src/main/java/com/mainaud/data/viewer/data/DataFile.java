package com.mainaud.data.viewer.data;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A data file.
 */
public final class DataFile {
    private Path path;
    private Connection connection;
    private SortedSet<DataTable> tables = new ConcurrentSkipListSet<>();

    private DataFile() {
    }

    public Path getPath() {
        return path;
    }

    public Connection getConnection() {
        return connection;
    }

    public Set<DataTable> getTables() {
        return tables;
    }

    public static DataFile create(Consumer<Schema> builder) {
        Schema schema = new Schema();
        builder.accept(schema);
        return schema.file;
    }

    public static final class Schema {
        DataFile file = new DataFile();

        public Schema path(Path path) {
            file.path = requireNonNull(path);
            return this;
        }

        public Schema connection(Connection connection) {
            file.connection = requireNonNull(connection);
            return this;
        }

        public Schema createTable(Consumer<DataTable.Schema> table) {
            file.tables.add(DataTable.create(table.andThen(t -> t.file(file))));
            return this;
        }
    }
}
