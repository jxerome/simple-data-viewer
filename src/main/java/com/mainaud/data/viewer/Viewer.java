package com.mainaud.data.viewer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.codestory.http.WebServer;
import net.codestory.http.injection.GuiceAdapter;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class Viewer {
    @Parameter(names = {"-p", "--port"}, description = "HTTP server port")
    private int port = 8080;

    @Parameter(names = {"-h", "--help"}, description = "Print Help", help = true)
    private boolean help;

    @Parameter(description = "Files", required = false)
    private List<String> files = Collections.emptyList();

    /**
     * When {@true} the browser is not launched at startup. Usefull for testing.
     */
    private boolean noBrowser;
    private Injector injector;
    private WebServer webServer;

    public int getPort() {
        return port;
    }

    public void randomPort() {
        this.port = 0;
    }

    public void noBrowser() {
        this.noBrowser = true;
    }

    public static void main(String[] args) {
        Viewer viewer = new Viewer();

        JCommander jCommander = new JCommander(viewer);
        try {
            jCommander.parse(args);

            if (viewer.help) {
                jCommander.usage();
            } else {
                viewer.start();
            }
        } catch (ParameterException e) {
            System.err.println("Invalid parameter : " + e.getMessage());
            System.err.println("   -h, --help for help");
        }
    }

    public void start() {
        createInjector();
        if (checkFiles()) {
            startRestServer();
            if (!noBrowser) {
                openWebPage();
            }
        }
    }

    public void stop() {
        webServer.stop();
    }

    private void createInjector() {
        injector = Guice.createInjector(new ViewerModule());
    }

    private boolean checkFiles() {
        FileService fileService = injector.getInstance(FileService.class);

        long countInvalidFiles = files.stream()
            .map(Paths::get)
            .filter(p -> !fileService.checkFile(p))
            .peek(p -> System.err.println("Invalid file " + p))
            .count();

        if (countInvalidFiles > 0) {
            System.err.println(String.format("%d invalid files", countInvalidFiles));
        }
        return countInvalidFiles == 0;
    }

    private void startRestServer() {
        webServer = new WebServer();
        webServer.configure(routes -> routes.setIocAdapter(new GuiceAdapter(injector)));

        if (port == 0) {
            webServer.startOnRandomPort();
            port = webServer.port();
        } else {
            webServer.start(port);
        }
    }

    private void openWebPage() {
        String appURI = String.format("http://localhost:%d/", port);
        System.out.println("Connect to " + appURI);

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(URI.create(appURI));

                } catch (IOException e) {
                    System.err.println("Cannot start browser : " + e.getMessage());
                }
            }
        }
    }
}
