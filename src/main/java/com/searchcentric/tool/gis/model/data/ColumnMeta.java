package com.searchcentric.tool.gis.model.data;

/**
 * Created by undwood on 11.08.16.
 */
public class ColumnMeta {

    private String name;
    private String alias;
    private Class clazz;

    public ColumnMeta() {
    }

    public ColumnMeta(String name, String alias, Class clazz) {
        this.name = name;
        this.alias = alias;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
