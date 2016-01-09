package com.mainaud.data.viewer;

import net.codestory.rest.FluentRestTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ViewerRestApiTest implements FluentRestTest {

    private Viewer viewer;

    @BeforeClass
    private void startViewer() {
        viewer = new Viewer();
        viewer.randomPort();
        viewer.noBrowser();
        viewer.start();
    }

    @AfterClass(alwaysRun = true)
    private void stopViewer() {
        viewer.stop();
    }

    @Override
    public int port() {
        return viewer.getPort();
    }

    @Test
    public void rootShouldMainPage() {
        get("/").should().succeed().contain("<h1>Simple Data Viewer</h1>");

    }
}
