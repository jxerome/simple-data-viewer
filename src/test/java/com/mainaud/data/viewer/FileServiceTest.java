package com.mainaud.data.viewer;

import com.mainaud.data.viewer.data.DataColumn;
import com.mainaud.data.viewer.data.DataFile;
import com.mainaud.data.viewer.data.DataTable;
import com.mainaud.data.viewer.data.DataType;
import com.mainaud.function.Result;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class FileServiceTest {
    private FileService fileService;

    @BeforeClass
    public void init() {
        fileService = new FileService();
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
        boolean result = fileService.checkFile(getPath(fileName));
        assertThat(result).isEqualTo(expectedValue);
    }

    @Test
    public void openFileShouldExtractSchemaInfo() throws SQLException, URISyntaxException {
        try {
            Path dbpath = getPath("empty.db");
            Result<Void, Path> result = fileService.openFile(dbpath);

            assertThat(result.isSuccess()).isTrue();
            assertThat(fileService.getFile(dbpath)).isPresent();

            DataFile actualFile = fileService.getFile(dbpath).get();
            assertThat(actualFile.getPath()).isEqualTo(dbpath);
            assertThat(actualFile.getConnection().isValid(10000));

            SortedSet<DataTable> tables = actualFile.getTables();
            assertThat(tables).extracting(DataTable::getName).containsExactly("compta", "personne");
            assertThat(tables).extracting(DataTable::getFile).contains(actualFile, actualFile);

            List<DataColumn> columns = tables.stream().flatMap(t -> t.getColumns().stream()).collect(Collectors.toList());
            assertThat(columns)
                .extracting(DataColumn::getName, DataColumn::getType)
                .containsExactly(
                    tuple("montant", DataType.VALUE),
                    tuple("variable1", DataType.VARIABLE),
                    tuple("variable2", DataType.VARIABLE),
                    tuple("age", DataType.VALUE),
                    tuple("nom", DataType.VARIABLE),
                    tuple("prenom", DataType.VARIABLE));


        } finally {
            fileService.close();
        }
    }
}
