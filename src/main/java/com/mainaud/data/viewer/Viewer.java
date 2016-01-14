package com.mainaud.data.viewer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.mainaud.function.Result;
import net.codestory.http.WebServer;
import net.codestory.http.injection.SpringAdapter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.inject.Inject;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Viewer {
    @Parameter(names = {"-p", "--port"}, description = "HTTP server port")
    private int port = 8080;

    @Parameter(names = {"-h", "--help"}, description = "Print Help", help = true)
    private boolean help;

    @Parameter(description = "Files", required = true)
    private List<Path> files = new ArrayList<>();

    /**
     * When {@true} the browser is not launched at startup. Usefull for testing.
     */
    private boolean noBrowser;
    private ConfigurableApplicationContext context;
    private WebServer webServer;

    @Inject
    private FileService fileService;

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
        initContainer();
        if (openFiles()) {
            startRestServer();
            if (!noBrowser) {
                openWebPage();
            }
        }
    }

    public void stop() {
        if (webServer != null) {
            webServer.stop();
        }
        if (context != null && context.isActive()) {
            context.close();
        }
    }

    private void initContainer() {
        context = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        context.registerShutdownHook();
        context.getBeanFactory().autowireBean(this);
    }

    private boolean openFiles() {
        long countInvalidFiles = files.stream()
            .map(fileService::openFile)
            .filter(Result::isFailure)
            .peek(p -> System.err.println("Invalid file " + p.getError()))
            .count();

        if (countInvalidFiles > 0) {
            System.err.println(String.format("%d invalid files", countInvalidFiles));
        }

        return countInvalidFiles == 0;
    }

    private void startRestServer() {
        webServer = new WebServer();
        webServer.configure(routes -> routes.setIocAdapter(new SpringAdapter(context)));

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
