package com.mainaud.data.viewer.data;

import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class DataTableTest {

    @Test
    public void createShouldCreateDataTable() {
        DataFile file = DataFile.create(schema -> schema.path(Paths.get("path")));
        DataTable table = DataTable.create(schema -> schema.name("name").file(file).createColumn(c -> c.name("col").type(DataType.OTHER)));

        assertThat(table.getName()).isEqualTo("name");
        assertThat(table.getFile()).isSameAs(file);
        assertThat(table.getColumns()).hasSize(1);

        DataColumn actualColumn = table.getColumns().first();
        assertThat(actualColumn.getName()).isEqualTo("col");
        assertThat(actualColumn.getType()).isEqualTo(DataType.OTHER);
        assertThat(actualColumn.getTable()).isSameAs(table);
    }

    @Test
    public void createShouldCreateAllColumns() {
        DataFile file = DataFile.create(schema -> schema.path(Paths.get("path")));
        DataTable table = DataTable.create(schema ->
            schema.name("name")
                .file(file)
                .createColumn(c -> c.name("a").type(DataType.VARIABLE))
                .createColumn(c -> c.name("b").type(DataType.VALUE)));

        assertThat(table.getName()).isEqualTo("name");
        assertThat(table.getFile()).isSameAs(file);
        assertThat(table.getColumns()).hasSize(2);

        DataColumn firstColumn = table.getColumns().first();
        assertThat(firstColumn.getName()).isEqualTo("a");
        assertThat(firstColumn.getType()).isEqualTo(DataType.VARIABLE);
        assertThat(firstColumn.getTable()).isSameAs(table);

        DataColumn lastColumn = table.getColumns().last();
        assertThat(lastColumn.getName()).isEqualTo("b");
        assertThat(lastColumn.getType()).isEqualTo(DataType.VALUE);
        assertThat(lastColumn.getTable()).isSameAs(table);
    }
}
