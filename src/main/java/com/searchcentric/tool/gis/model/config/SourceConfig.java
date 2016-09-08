package com.searchcentric.tool.gis.model.config;

/**
 * Created by undwood on 11.08.16.
 */
public class SourceConfig {
    public ConnectionConfig connection;
    public QueryConfig query;

    public DestinationConfig destinationConfig;


    public ConnectionConfig getConnection() {
        return connection;
    }

    public void setConnection(ConnectionConfig connection) {
        this.connection = connection;
    }

    public QueryConfig getQuery() {
        return query;
    }

    public void setQuery(QueryConfig query) {
        this.query = query;
    }
}
