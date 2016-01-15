package com.mainaud.data.viewer.dto;

public class Table {
    private String name;
    private String file;
    private String folder;


    public Table() {
    }

    public Table(String name, String file, String folder) {
        this.name = name;
        this.file = file;
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
