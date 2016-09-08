package com.searchcentric.tool.gis.model.config;

/**
 * Created by undwood on 11.08.16.
 */
public class ColumnConfig {
    public String name;
    public String mapping;
    public String value;
    public String type;
    public String date_pattern;
    public Boolean is_geometry;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate_pattern() {
        return date_pattern;
    }

    public void setDate_pattern(String date_pattern) {
        this.date_pattern = date_pattern;
    }

    public Boolean getIs_geometry() {
        return is_geometry;
    }

    public void setIs_geometry(Boolean is_geometry) {
        this.is_geometry = is_geometry;
    }
}
