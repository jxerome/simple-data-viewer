package com.mainaud.data.viewer.data.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mainaud.data.viewer.data.WithIdBuilder;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A data withFile.
 */
public final class DataFile implements WithFile {
    private UUID id;
    private Path path;
    private Connection connection;
    private final List<DataTable> tables = new ArrayList<>();

    private DataFile() {
    }

    public UUID getId() {
        return id;
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

    public static final class Schema implements WithIdBuilder<Schema> {
        DataFile file = new DataFile();

        @Override
        public Schema withId(UUID id) {
            file.id = requireNonNull(id);
            return this;
        }

        public Schema withPath(Path path) {
            file.path = requireNonNull(path);
            return this;
        }

        public Schema withConnection(Connection connection) {
            file.connection = requireNonNull(connection);
            return this;
        }

        public Schema createTable(Consumer<DataTable.Schema> builder) {
            file.tables.add(DataTable.create(t -> {
                t.withFile(file);
                builder.accept(t);
            }));
            return this;
        }
    }
}
