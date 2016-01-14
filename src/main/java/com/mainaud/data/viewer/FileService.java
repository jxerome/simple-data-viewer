package com.mainaud.data.viewer;

import com.mainaud.data.viewer.data.DataFile;
import com.mainaud.data.viewer.data.DataType;
import com.mainaud.function.ConsumerWithException;
import com.mainaud.function.Result;
import com.mainaud.function.TryTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Service that check and load files.
 */
@Singleton
@Service
public class FileService {
    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    public static final byte[] SQLITE_HEADER = {0x53, 0x51, 0x4c, 0x69, 0x74, 0x65, 0x20, 0x66, 0x6f, 0x72, 0x6d, 0x61, 0x74, 0x20, 0x33, 0x00};

    private final SortedMap<Path, DataFile> dataFiles = new ConcurrentSkipListMap<>();

    /**
     * Check if the file is a valid sqlite file.
     */
    public boolean checkFile(Path path) {
        return Files.isReadable(path) && Files.isRegularFile(path) && isSqliteFile(path);
    }

    public Optional<DataFile> getFile(Path path) {
        return Optional.ofNullable(dataFiles.get(path));
    }

    /**
     * Open File and load meta data.
     *
     * @param path File path.
     * @return Result with failed path.
     */
    public Result<Void, Path> openFile(Path path) {
        if (!checkFile(path)) {
            return Result.failure(path);
        }

        try {
            dataFiles.computeIfAbsent(path, TryTo.apply(p -> {
                Connection connection = openConnection(path);

                return DataFile.create(TryTo.accept(schema -> {
                    schema.path(path);
                    schema.connection(connection);
                    executeSimpleQuery(connection, "SELECT name FROM sqlite_master WHERE type = 'table'", rst ->
                        schema.createTable(TryTo.accept(table -> {
                            String tableName = rst.getString("name");
                            table.name(tableName);
                            executeSimpleQuery(connection, "pragma table_info(" + tableName + ")", rsc ->
                                table.createColumn(TryTo.accept(column ->
                                    column.name(rsc.getString("name"))
                                        .type(DataType.of(rsc.getString("type")))
                                ))
                            );
                        }))
                    );
                }));
            }));

            return Result.success();

        } catch (CompletionException e) {
            LOG.error("Erreur de lecture du fichier {}", path, e);
            return Result.failure(path);
        }
    }

    private Connection openConnection(Path path) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:".concat(path.toString()));
    }

    private void executeSimpleQuery(Connection connection, String query, ConsumerWithException<ResultSet, SQLException> todo) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    todo.accept(rs);
                }
            }
        }
    }

    @PreDestroy
    public void close() {
        LOG.info("Close FileService");
        for (DataFile dataFile : dataFiles.values()) {
            try {
                dataFile.getConnection().close();
            } catch (SQLException e) {
                LOG.error("Error while closing {}", dataFile.getPath(), e);
            }
        }
        dataFiles.clear();
    }

    private boolean isSqliteFile(Path path) {
        try {
            try (InputStream in = Files.newInputStream(path)) {
                byte[] header = new byte[SQLITE_HEADER.length];
                int len = in.read(header);
                return len == SQLITE_HEADER.length && Arrays.equals(header, SQLITE_HEADER);
            }
        } catch (IOException e) {
            LOG.error("IO Exception while reading {}", path, e);
            return false;
        }
    }
}
