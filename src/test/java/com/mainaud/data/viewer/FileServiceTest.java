package com.mainaud.data.viewer;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class FileServiceTest {
    private FileService fileService;

    @BeforeClass
    public void init() {
        fileService = new FileService();
    }

    @DataProvider(name = "checkResultData")
    public Object[][] providesCheckResultData() {
        return new Object[][]{
            {"simple-text.txt", false},
            {"empty.db", true},
        };
    }


    @Test(dataProvider = "checkResultData")
    public void ensureCheckFileReturnGoodResult(String fileName, boolean expectedValue) throws URISyntaxException {
        URL url = getClass().getResource(fileName);
        boolean result = fileService.checkFile(Paths.get(url.toURI()));
        assertThat(result).isEqualTo(expectedValue);
    }

}
