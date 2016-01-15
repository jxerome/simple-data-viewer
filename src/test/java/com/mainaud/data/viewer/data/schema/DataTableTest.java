package com.mainaud.data.viewer.data.schema;

import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class DataTableTest {

    @Test
    public void createShouldCreateDataTable() {
        DataFile file = DataFile.create(schema -> schema.withPath(Paths.get("withPath")));
        DataTable table = DataTable.create(schema -> schema.withName("withName").withFile(file).createColumn(c -> c.withName("col").withType(DataType.OTHER)));

        assertThat(table.getName()).isEqualTo("withName");
        assertThat(table.getFile()).isSameAs(file);
        assertThat(table.getColumns()).hasSize(1);

        DataColumn actualColumn = table.getColumns().get(0);
        assertThat(actualColumn.getName()).isEqualTo("col");
        assertThat(actualColumn.getType()).isEqualTo(DataType.OTHER);
        assertThat(actualColumn.getTable()).isSameAs(table);
    }

    @Test
    public void createShouldCreateAllColumns() {
        DataFile file = DataFile.create(schema -> schema.withPath(Paths.get("withPath")));
        DataTable table = DataTable.create(schema ->
            schema.withName("withName")
                .withFile(file)
                .createColumn(c -> c.withName("a").withType(DataType.VARIABLE))
                .createColumn(c -> c.withName("b").withType(DataType.VALUE)));

        assertThat(table.getName()).isEqualTo("withName");
        assertThat(table.getFile()).isSameAs(file);
        assertThat(table.getColumns()).hasSize(2);

        DataColumn firstColumn = table.getColumns().get(0);
        assertThat(firstColumn.getName()).isEqualTo("a");
        assertThat(firstColumn.getType()).isEqualTo(DataType.VARIABLE);
        assertThat(firstColumn.getTable()).isSameAs(table);

        DataColumn lastColumn = table.getColumns().get(1);
        assertThat(lastColumn.getName()).isEqualTo("b");
        assertThat(lastColumn.getType()).isEqualTo(DataType.VALUE);
        assertThat(lastColumn.getTable()).isSameAs(table);
    }
}
