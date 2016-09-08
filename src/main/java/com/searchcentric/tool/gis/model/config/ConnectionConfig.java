package com.searchcentric.tool.gis.model.config;

/**
 * Created by undwood on 11.08.16.
 */
public class ConnectionConfig {
    public String jdbc_driver;
    public String jdbc_path;

    public String getJdbc_driver() {
        return jdbc_driver;
    }

    public void setJdbc_driver(String jdbc_driver) {
        this.jdbc_driver = jdbc_driver;
    }

    public String getJdbc_path() {
        return jdbc_path;
    }

    public void setJdbc_path(String jdbc_path) {
        this.jdbc_path = jdbc_path;
    }
}
