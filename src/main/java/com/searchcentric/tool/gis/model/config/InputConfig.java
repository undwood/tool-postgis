package com.searchcentric.tool.gis.model.config;

/**
 * Created by undwood on 11.08.16.
 */
public class InputConfig {
    public SourceConfig source;
    public DestinationConfig destination;

    public SourceConfig getSource() {
        return source;
    }

    public void setSource(SourceConfig source) {
        this.source = source;
    }

    public DestinationConfig getDestination() {
        return destination;
    }

    public void setDestination(DestinationConfig destination) {
        this.destination = destination;
    }
}
