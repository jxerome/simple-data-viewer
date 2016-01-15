package com.mainaud.data.viewer.data;

import com.mainaud.data.viewer.data.schema.DataColumn;

import java.util.UUID;

public class Column {
    private UUID id;
    private String name;
    private String type;

    public Column() {
    }

    public Column(DataColumn column) {
        id = column.getId();
        name = column.getName();
        type = column.getType().name();
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
