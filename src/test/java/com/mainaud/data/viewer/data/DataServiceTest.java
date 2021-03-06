package com.mainaud.data.viewer.data;

import com.mainaud.data.viewer.data.schema.DataColumn;
import com.mainaud.data.viewer.data.schema.DataFile;
import com.mainaud.data.viewer.data.schema.DataTable;
import com.mainaud.data.viewer.data.schema.DataType;
import com.mainaud.function.Result;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class DataServiceTest {
    private DataService dataService;
    private Result<Void, Path> result;
    private Path dbpath;

    @BeforeClass
    public void init() throws URISyntaxException, IOException {
        dataService = new DataService();
        dbpath = getPath("empty.db");
        result = dataService.openFile(dbpath);
    }

    @AfterClass(alwaysRun = true)
    public void close() {
        if (dataService != null) {
            dataService.close();
        }
    }

    @DataProvider(name = "checkResultData")
    public Object[][] providesCheckResultData() {
        return new Object[][]{
            {"simple-text.txt", false},
            {"empty.db", true},
        };
    }

    private Path getPath(String fileName) throws URISyntaxException {
        URL url = getClass().getResource(fileName);
        return Paths.get(url.toURI());
    }

    @Test(dataProvider = "checkResultData")
    public void ensureCheckFileReturnGoodResult(String fileName, boolean expectedValue) throws URISyntaxException {
        boolean result = dataService.checkFile(getPath(fileName));
        assertThat(result).isEqualTo(expectedValue);
    }

    @Test
    public void openFileShouldExtractSchemaInfo() throws SQLException, URISyntaxException {
        assertThat(result.isSuccess()).isTrue();
        assertThat(dataService.getFiles()).hasSize(1);

        DataFile actualFile = dataService.getFiles().get(0);
        assertThat(actualFile.getId()).isNotNull();
        assertThat(actualFile.getPath()).isEqualTo(dbpath);
        assertThat(actualFile.getConnection().isValid(10000));

        List<DataTable> tables = actualFile.getTables();
        assertThat(tables).extracting(DataTable::getId).doesNotContainNull();
        assertThat(tables)
            .extracting(DataTable::getName, DataTable::getFile)
            .containsExactly(
                tuple("compta", actualFile),
                tuple("personne", actualFile));

        List<DataColumn> columns = tables.stream().flatMap(t -> t.getColumns().stream()).collect(Collectors.toList());
        assertThat(columns).extracting(DataColumn::getId).doesNotContainNull();
        assertThat(columns)
            .extracting(DataColumn::getName, DataColumn::getType)
            .containsExactly(
                tuple("variable1", DataType.VARIABLE),
                tuple("variable2", DataType.VARIABLE),
                tuple("montant", DataType.VALUE),
                tuple("prenom", DataType.VARIABLE),
                tuple("nom", DataType.VARIABLE),
                tuple("age", DataType.VALUE),
                tuple("salaire", DataType.VALUE));
    }

    @Test
    public void listTablesShouldProvideTables() {
        assertThat(dataService.listTables())
            .extracting(Table::getName, Table::getFile, Table::getFolder)
            .containsExactly(
                tuple("compta", "empty.db", dbpath.getParent().toString()),
                tuple("personne", "empty.db", dbpath.getParent().toString()));
    }

    @Test
    public void listVariableColumnsShouldProvideVariables() {
        Optional<DataTable> table = dataService.getFiles().get(0).getTables().stream().filter(t -> t.getName().equals("personne")).findFirst();
        UUID id = table.get().getId();

        List<Column> actual = dataService.listVariableColumns(id);

        assertThat(actual)
            .extracting(Column::getName, Column::getType)
            .containsExactly(
                tuple("nom", "VARIABLE"),
                tuple("prenom", "VARIABLE"));
    }

    @Test
    public void listValueColumnsShouldProvideValues() {
        Optional<DataTable> table = dataService.getFiles().get(0).getTables().stream().filter(t -> t.getName().equals("personne")).findFirst();
        UUID id = table.get().getId();

        List<Column> actual = dataService.listValueColumns(id);

        assertThat(actual)
            .extracting(Column::getName, Column::getType)
            .containsExactly(
                tuple("age", "VALUE"),
                tuple("salaire", "VALUE"));
    }
}
