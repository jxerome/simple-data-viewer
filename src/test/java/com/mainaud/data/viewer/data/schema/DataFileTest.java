package com.mainaud.data.viewer.data.schema;


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
            schema.withPath(Paths.get("withPath"))
                .withConnection(connection)
                .createTable(t -> t.withName("t1"))
                .createTable(t -> t.withName("t2"))
        );

        assertThat(file.getFile()).isSameAs(file);
        assertThat(file.getPath()).isEqualTo(Paths.get("withPath"));
        assertThat(file.getConnection()).isSameAs(connection);
        assertThat(file.getTables()).extracting(t -> t.getName()).containsExactly("t1", "t2");
        assertThat(file.getTables()).extracting(t -> t.getFile()).containsExactly(file, file);


    }
}
