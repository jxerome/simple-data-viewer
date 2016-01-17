package com.mainaud.data.viewer.data;

import com.google.common.io.CharStreams;
import com.mainaud.data.viewer.data.schema.DataColumn;
import com.mainaud.data.viewer.data.schema.DataFile;
import com.mainaud.data.viewer.data.schema.DataTable;
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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * Service that check and load files.
 */
@Singleton
@Service
public class DataService {
    private static final byte[] SQLITE_HEADER = {0x53, 0x51, 0x4c, 0x69, 0x74, 0x65, 0x20, 0x66, 0x6f, 0x72, 0x6d, 0x61, 0x74, 0x20, 0x33, 0x00};
    private static final int LIMIT = 100;

    private static final Logger LOG = LoggerFactory.getLogger(DataService.class);

    private final List<DataFile> dataFiles = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<UUID, DataTable> dataTables = new ConcurrentHashMap<>();
    private final String statQuery;

    public DataService() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("stats.sql");
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8)
        ) {
            statQuery = CharStreams.toString(isr);
        }
    }

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
            dataFile.getTables().forEach(t -> dataTables.put(t.getId(), t));

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
        return dataTables.values().stream()
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

    public Result<Stats, Exception> computeStats(UUID tableId, String variableName, String valueName) {
        return getDataTable(tableId)
            .map(t -> t
                .getColumn(variableName).map(variable -> t
                    .getColumn(valueName).map(value ->
                        computeStats(t, variable, value))

                    .orElseGet(() -> Result.failure(new NoSuchElementException("Invalid value name"))))
                .orElseGet(() -> Result.failure(new NoSuchElementException("Invalid variable name"))))
            .orElseGet(() -> Result.failure(new NoSuchElementException("Invalid table id")));
    }

    private Result<Stats, Exception> computeStats(DataTable table, DataColumn variable, DataColumn value) {
        /*
         * Prepare the query.
         * The implementation is naive and could be replace by a template engine but it works well.
         */
        String query = statQuery.replaceAll("€TABLE_NAME€", table.getName())
            .replaceAll("€VARIABLE_NAME€", variable.getName())
            .replaceAll("€VALUE_NAME€", value.getName());

        try {
            Stats stats = new Stats();
            stats.setLines(new ArrayList<>(LIMIT));

            executeSimpleQuery(table.getFile().getConnection(), query, rs -> {
                switch (rs.getInt(1)) {
                    case 1:
                        stats.getLines().add(new Stat(rs.getString(2), rs.getLong(3), rs.getDouble(4)));
                        break;
                    case 2:
                        if (rs.getLong(3) > 0) {
                            stats.setOthers(new Stat(null, rs.getLong(3), rs.getDouble(4)));
                        }
                        break;
                    default:
                        throw new IllegalStateException("Invalid line type " + rs.getInt(1));
                }
            });

            return Result.success(stats);

        } catch (SQLException | IllegalStateException e) {
                LOG.error("Error while query stats for table «{}», variable «{}» and value «{}»", table.getName(), variable.getName(), value.getName());
            return Result.failure(e);
        }
    }

    private List<Column> listColumns(UUID tableId, DataType type) {
        return getDataTable(tableId)
            .map(t -> t.getColumns().stream()
                .filter(c -> c.getType().equals(type))
                .map(Column::new)
                .sorted(comparing(Column::getName))
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    private Optional<DataTable> getDataTable(UUID id) {
        return Optional.ofNullable(dataTables.get(id));
    }

}
