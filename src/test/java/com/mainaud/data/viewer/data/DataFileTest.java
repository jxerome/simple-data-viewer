package com.mainaud.data.viewer.data;


import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

public class DataFileTest {

    @Test
    public void createShouldCreateFile() {
        Connection connection = Mockito.mock(Connection.class);

        DataFile file = DataFile.create(schema ->
            schema.path(Paths.get("path"))
                .connection(connection)
                .createTable(t -> t.name("t1"))
                .createTable(t -> t.name("t2"))
        );

        assertThat(file.getFile()).isSameAs(file);
        assertThat(file.getPath()).isEqualTo(Paths.get("path"));
        assertThat(file.getConnection()).isSameAs(connection);
        assertThat(file.getTables()).extracting(t -> t.getName()).containsExactly("t1", "t2");
        assertThat(file.getTables()).extracting(t -> t.getFile()).containsExactly(file, file);


    }
}
