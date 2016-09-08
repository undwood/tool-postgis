package com.searchcentric.tool.gis.model.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by undwood on 11.08.16.
 */
public class ExtractedData {
    private Map<String, ColumnMeta> mapColumnMeta;
    private Map<String, Object> mapData;

    public ExtractedData() {
        this.mapColumnMeta = new HashMap<>();
        this.mapData = new HashMap<>();
    }

    public Map<String, ColumnMeta> getMapColumnMeta() {
        return mapColumnMeta;
    }

    public void setMapColumnMeta(Map<String, ColumnMeta> mapColumnMeta) {
        this.mapColumnMeta = mapColumnMeta;
    }

    public Map<String, Object> getMapData() {
        return mapData;
    }

    public void setMapData(Map<String, Object> mapData) {
        this.mapData = mapData;
    }
}
