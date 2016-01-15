package com.mainaud.data.viewer;

import com.mainaud.data.viewer.data.Column;
import com.mainaud.data.viewer.data.Table;

import java.util.List;

public class Response {
    private List<Table> tables;
    private List<Column> columns;

    public Response() {
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public Response withTables(List<Table> tables) {
        setTables(tables);

        return this;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public Response withColumns(List<Column> columns) {
        setColumns(columns);
        return this;
    }
}
