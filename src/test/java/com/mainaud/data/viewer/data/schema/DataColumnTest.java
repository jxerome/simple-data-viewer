package com.mainaud.data.viewer.data.schema;

import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class DataColumnTest {
    @Test
    public void createShouldGiveDataColumn() {
        DataFile file = DataFile.create(schema -> schema.withPath(Paths.get("withPath")));
        DataTable table = DataTable.create(schema -> schema.withName("table").withFile(file));

        DataColumn column = DataColumn.create(schema -> schema.withName("withName").withType(DataType.VALUE).withTable(table));
        assertThat(column.getName()).isEqualTo("withName");
        assertThat(column.getType()).isEqualTo(DataType.VALUE);
        assertThat(column.getTable()).isSameAs(table);
        assertThat(column.getFile()).isSameAs(file);
    }
}
