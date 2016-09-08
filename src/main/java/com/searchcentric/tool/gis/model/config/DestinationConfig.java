package com.searchcentric.tool.gis.model.config;

/**
 * Created by undwood on 11.08.16.
 */
public class DestinationConfig {
    public ConnectionConfig connection;
    public String srid;
    public String sequence;
    public String type;
    public String lat_x;
    public String lon_y;
    public DestinationTableConfig destination_table;

    public ConnectionConfig getConnection() {
        return connection;
    }

    public void setConnection(ConnectionConfig connection) {
        this.connection = connection;
    }

    public String getSrid() {
        return srid;
    }

    public void setSrid(String srid) {
        this.srid = srid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DestinationTableConfig getDestination_table() {
        return destination_table;
    }

    public void setDestination_table(DestinationTableConfig destination_table) {
        this.destination_table = destination_table;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getLat_x() {
        return lat_x;
    }

    public void setLat_x(String lat_x) {
        this.lat_x = lat_x;
    }

    public String getLon_y() {
        return lon_y;
    }

    public void setLon_y(String lon_y) {
        this.lon_y = lon_y;
    }
}
