package com.mainaud.data.viewer.data;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Objects;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A data file.
 */
public final class DataFile implements InFile, Comparable<DataFile> {
    private Path path;
    private Connection connection;
    private final SortedSet<DataTable> tables = new ConcurrentSkipListSet<>();

    private DataFile() {
    }

    public Path getPath() {
        return path;
    }

    public Connection getConnection() {
        return connection;
    }

    public SortedSet<DataTable> getTables() {
        return tables;
    }

    @Override
    public DataFile getFile() {
        return this;
    }

    @Override
    public int compareTo(DataFile that) {
        return this.path.compareTo(that.path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataFile)) return false;
        DataFile that = (DataFile) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
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
            file.tables.add(DataTable.create(t -> { t.file(file); builder.accept(t); }));
            return this;
        }
    }
}
