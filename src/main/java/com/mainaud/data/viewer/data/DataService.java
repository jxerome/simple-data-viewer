package com.mainaud.data.viewer.data;

import com.mainaud.data.viewer.data.schema.DataFile;
import com.mainaud.data.viewer.data.schema.DataType;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * Service that check and load files.
 */
@Singleton
@Service
public class DataService {
    private static final Logger LOG = LoggerFactory.getLogger(DataService.class);

    public static final byte[] SQLITE_HEADER = {0x53, 0x51, 0x4c, 0x69, 0x74, 0x65, 0x20, 0x66, 0x6f, 0x72, 0x6d, 0x61, 0x74, 0x20, 0x33, 0x00};

    private final List<DataFile> dataFiles = new CopyOnWriteArrayList<>();

    /**
     * Check if the withFile is a valid sqlite withFile.
     */
    public boolean checkFile(Path path) {
        return Files.isReadable(path) && Files.isRegularFile(path) && isSqliteFile(path);
    }

    public List<DataFile> getFiles() {
        return dataFiles;
    }

    /**
     * Open File and load meta data.
     *
     * @param path File withPath.
     * @return Result with failed withPath.
     */
    public Result<Void, Path> openFile(Path path) {
        if (!checkFile(path)) {
            return Result.failure(path);
        }

        try {
            Connection connection = openConnection(path);

            DataFile dataFile = DataFile.create(TryTo.accept(schema -> {
                schema.withRandomId();
                schema.withPath(path);
                schema.withConnection(connection);
                executeSimpleQuery(connection, "SELECT name FROM sqlite_master WHERE type = 'table'", rst ->
                    schema.createTable(TryTo.accept(table -> {
                        table.withRandomId();
                        String tableName = rst.getString("name");
                        table.withName(tableName);
                        executeSimpleQuery(connection, "pragma table_info(" + tableName + ")", rsc ->
                            table.createColumn(TryTo.accept(column ->
                                column.withRandomId()
                                    .withName(rsc.getString("name"))
                                    .withType(DataType.of(rsc.getString("type")))
                            ))
                        );
                    }))
                );
            }));
            dataFiles.add(dataFile);

            return Result.success();

        } catch (CompletionException | ClassNotFoundException | SQLException e) {
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
        for (DataFile dataFile : dataFiles) {
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

    public List<Table> listTables() {
        return dataFiles.stream()
            .flatMap(f -> f.getTables().stream())
            .map(Table::new)
            .sorted(comparing(Table::getName)
                .thenComparing(Table::getFile)
                .thenComparing(Table::getFolder, Comparator.nullsFirst(Comparator.naturalOrder())))
            .collect(Collectors.toList());
    }

    public List<Column> listVariableColumns(UUID tableId) {
        return listColumns(tableId, DataType.VARIABLE);
    }

    public List<Column> listValueColumns(UUID tableId) {
        return listColumns(tableId, DataType.VALUE);
    }

    private List<Column> listColumns(UUID tableId, DataType type) {
        return dataFiles.stream()
                .flatMap(f -> f.getTables().stream())
                .filter(t -> t.getId().equals(tableId))
                .flatMap(t -> t.getColumns().stream())
                .filter(c -> c.getType().equals(type))
                .map(Column::new)
                .sorted(comparing(Column::getName))
                .collect(Collectors.toList());
    }
}
