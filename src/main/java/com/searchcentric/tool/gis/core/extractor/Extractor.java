package com.searchcentric.tool.gis.core.extractor;

import com.kadme.tool.log.Reporter;
import com.searchcentric.tool.gis.model.config.InputConfig;
import com.searchcentric.tool.gis.model.config.SourceConfig;
import com.searchcentric.tool.gis.model.data.ColumnMeta;
import com.searchcentric.tool.gis.model.data.ExtractedData;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by undwood on 11.08.16.
 */
public abstract class Extractor {

    public  abstract ExtractedData extract(SourceConfig config, Reporter reporter);
    protected Map<String, ColumnMeta> getColumnsMeta(ResultSetMetaData md) throws SQLException, ClassNotFoundException {
        Map<String, ColumnMeta> columnMeta = new HashMap<>();
        int columnCount = md.getColumnCount();
        for(int i = 1; i <= columnCount; i++) {
            columnMeta.put(md.getColumnName(i),
                    new ColumnMeta(md.getColumnName(i), md.getColumnLabel(i), Class.forName(md.getColumnClassName(i))));
        }
        return columnMeta;
    }
}
