package com.mainaud.data.viewer;

import com.mainaud.data.viewer.data.Table;

import java.util.List;

public class Response {
    private List<Table> tables;

    public Response() {
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public Response withTables(List<Table> tables) {
        this.tables = tables;
        return this;
    }
}
