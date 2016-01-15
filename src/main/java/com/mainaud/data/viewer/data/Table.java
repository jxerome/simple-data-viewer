package com.mainaud.data.viewer.data;

import com.mainaud.data.viewer.data.schema.DataTable;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public final class Table implements WithId {
    private UUID id;
    private String name;
    private String file;
    private String folder;

    public Table() {
    }

    public Table(DataTable table) {
        setId(table.getId());
        setName(table.getName());

        Path path = table.getFile().getPath();
        setFile(path.getFileName().toString());

        Path folder = path.getParent();
        if (folder != null) {
            setFolder(folder.toString());
        }
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Table)) return false;
        Table table = (Table) o;
        return Objects.equals(id, table.id) &&
            Objects.equals(name, table.name) &&
            Objects.equals(file, table.file) &&
            Objects.equals(folder, table.folder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, file, folder);
    }
}
