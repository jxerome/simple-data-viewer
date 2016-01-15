package com.mainaud.data.viewer.schema;

import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class DataColumnTest {
    @Test
    public void createShouldGiveDataColumn() {
        DataFile file = DataFile.create(schema -> schema.path(Paths.get("path")));
        DataTable table = DataTable.create(schema -> schema.name("table").file(file));

        DataColumn column = DataColumn.create(schema -> schema.name("name").type(DataType.VALUE).table(table));
        assertThat(column.getName()).isEqualTo("name");
        assertThat(column.getType()).isEqualTo(DataType.VALUE);
        assertThat(column.getTable()).isSameAs(table);
        assertThat(column.getFile()).isSameAs(file);
    }
}
