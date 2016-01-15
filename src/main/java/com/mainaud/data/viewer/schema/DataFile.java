package com.mainaud.data.viewer.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A data file.
 */
public final class DataFile implements InFile {
    private Path path;
    private Connection connection;
    private final List<DataTable> tables = new ArrayList<>();

    private DataFile() {
    }

    public Path getPath() {
        return path;
    }

    @JsonIgnore
    public Connection getConnection() {
        return connection;
    }

    @JsonIgnore
    public List<DataTable> getTables() {
        return tables;
    }

    @Override
    public DataFile getFile() {
        return this;
    }

    public static DataFile create(Consumer<Schema> builder) {
        Schema schema = new Schema();
        builder.accept(schema);

        DataFile file = schema.file;
        requireNonNull(file.path);
        return file;
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

        public Schema createTable(Consumer<DataTable.Schema> builder) {
            file.tables.add(DataTable.create(t -> {
                t.file(file);
                builder.accept(t);
            }));
            return this;
        }
    }
}
