package com.mainaud.data.viewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Service that check and load files.
 */
@Singleton
public class FileService {
    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    public static final byte[] SQLITE_HEADER = {0x53, 0x51, 0x4c, 0x69, 0x74, 0x65, 0x20, 0x66, 0x6f, 0x72, 0x6d, 0x61, 0x74, 0x20, 0x33, 0x00};


    /**
     * Check if the file is a valid sqlite file.
     */
    public boolean checkFile(Path path) {
        return Files.isReadable(path) && Files.isRegularFile(path) && isSqliteFile(path);
    }

    private boolean isSqliteFile(Path path) {
        try {
            try (InputStream in = Files.newInputStream(path)) {
                byte[] header = new byte[SQLITE_HEADER.length];
                int len = in.read(header);
                return len == SQLITE_HEADER.length && Arrays.equals(header, SQLITE_HEADER);
            }
        } catch (IOException e) {
            LOG.error("IO Exception while reading {} : {}", path, e.getMessage());
            return false;
        }
    }
}
