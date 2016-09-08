package com.searchcentric.tool.gis.util;

import com.kadme.tool.log.Reporter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by undwood on 11.08.16.
 */
public class ConnectionFactory {

    private Reporter reporter;

    public ConnectionFactory(Reporter reporter) {
        this.reporter = reporter;
    }


    public Connection getConnection(String jdbcClass, String jdbcPath) {
        Connection conn = null;
        try {
            Class.forName(jdbcClass);
            conn = DriverManager.getConnection(jdbcPath);
        } catch (SQLException se) {
            reporter.reportError(se.getMessage(),se);
        } catch (Exception e) {
            reporter.reportError(e.getMessage(),e);
        }
        return conn;
    }
}
