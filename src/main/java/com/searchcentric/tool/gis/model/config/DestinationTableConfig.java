package com.searchcentric.tool.gis.model.config;

import java.util.List;

/**
 * Created by undwood on 11.08.16.
 */
public class DestinationTableConfig {
    public String name;
    public String create_inline;
    public String create;
    public String truncate;
    public String truncate_inline;
    public List<ColumnConfig> columns;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreate_inline() {
        return create_inline;
    }

    public void setCreate_inline(String create_inline) {
        this.create_inline = create_inline;
    }

    public String getCreate() {
        return create;
    }

    public void setCreate(String create) {
        this.create = create;
    }

    public String getTruncate() {
        return truncate;
    }

    public void setTruncate(String truncate) {
        this.truncate = truncate;
    }

    public String getTruncate_inline() {
        return truncate_inline;
    }

    public void setTruncate_inline(String truncate_inline) {
        this.truncate_inline = truncate_inline;
    }

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }
}
