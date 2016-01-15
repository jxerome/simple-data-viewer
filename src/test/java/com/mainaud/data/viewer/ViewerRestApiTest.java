package com.mainaud.data.viewer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainaud.data.viewer.data.Table;
import net.codestory.rest.FluentRestTest;
import net.codestory.rest.RestAssert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class ViewerRestApiTest implements FluentRestTest {

    private Viewer viewer;
    private Path dbpath;

    @BeforeClass
    private void startViewer() throws URISyntaxException {
        viewer = new Viewer();
        viewer.randomPort();
        viewer.noBrowser();
        dbpath = getPath("com/mainaud/data/viewer/data/empty.db");
        viewer.addFile(dbpath);
        viewer.start();
    }

    @AfterClass(alwaysRun = true)
    private void stopViewer() {
        viewer.stop();
    }

    private Path getPath(String fileName) throws URISyntaxException {
        URL url = getClass().getClassLoader().getResource(fileName);
        return Paths.get(url.toURI());
    }

    @Override
    public int port() {
        return viewer.getPort();
    }

    @Test
    public void rootShouldMainPage() {
        get("/").should().succeed().contain("<h1>Simple Data Viewer</h1>");
    }

    @Test
    public void tablesShouldListTables() throws IOException {
        RestAssert restAssert = get("/table");
        restAssert.should().succeed().haveType("application/json");

        String content = restAssert.response().content();

        Response tables = new ObjectMapper().readValue(content, Response.class);
        assertThat(tables.getTables())
            .extracting(Table::getName, Table::getFile, Table::getFolder)
            .containsOnly(
                tuple("compta", "empty.db", dbpath.getParent().toString()),
                tuple("personne", "empty.db", dbpath.getParent().toString()));


    }
}
