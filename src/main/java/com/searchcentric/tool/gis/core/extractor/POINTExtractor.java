package com.searchcentric.tool.gis.core.extractor;


import com.kadme.tool.log.Reporter;
import com.searchcentric.tool.gis.core.builder.Builder;
import com.searchcentric.tool.gis.model.config.QueryConfig;
import com.searchcentric.tool.gis.model.config.SourceConfig;
import com.searchcentric.tool.gis.model.data.ExtractedData;
import com.searchcentric.tool.gis.util.ConnectionFactory;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by undwood on 11.08.16.
 */
public class POINTExtractor extends Extractor {


    @Override
    public ExtractedData extract(SourceConfig source, Reporter reporter) {
        ExtractedData extData = new ExtractedData();
        ConnectionFactory cf = new ConnectionFactory(reporter);
        Connection srcCon = cf.getConnection(source.connection.jdbc_driver, source.connection.jdbc_path);

        // perform query to extract main data
        // no groups, one row - one geo object
        QueryConfig query = source.query;
        String sql = "";
        String mainSql = "";
        try {
            if (StringUtils.isNotBlank(query.sql_inline)) {
                mainSql = query.sql_inline;
                sql = query.sql_inline + query.limit_clause;
            } else if (StringUtils.isNotBlank(query.sql)) {
                mainSql = new String(Files.readAllBytes(Paths.get(query.sql)));
                sql = new String(Files.readAllBytes(Paths.get(query.sql))) + query.limit_clause;
            } else {
                reporter.reportError("No SQL for source!");
                System.exit(1);
            }
        } catch (IOException e) {
            reporter.reportError(e.getMessage(), e);
            System.exit(1);
        }

        if (StringUtils.isNotBlank(sql)) {

            //get metadata from the query
            try {
                Statement stm = srcCon.createStatement();
                ResultSet rs = stm.executeQuery(sql);
                extData.setMapColumnMeta(getColumnsMeta(rs.getMetaData()));
                rs.close();
                stm.close();
            } catch (SQLException | ClassNotFoundException e) {
                reporter.reportError(e.getMessage(), e);
                System.exit(1);
            }

            // perform query for each grouping field
            try {
                Builder builder= (Builder) Class.forName("com.searchcentric.tool.gis.core.builder." + source.destinationConfig.type + "Builder").newInstance();
                builder.init(source.destinationConfig, reporter);

                int cur = 1;

                reporter.reportInfo("Requesting GIS points");

                Statement stm = srcCon.createStatement();
                ResultSet rs = stm.executeQuery(mainSql);
                int totalCount = rs.getFetchSize();
                while (rs.next()) {
                    reporter.reportInfo("Extracted " + cur + " of " + totalCount);
                    Map<String, Object> valsMap = new HashMap<String, Object>();
                    for (String colName : extData.getMapColumnMeta().keySet()) {
                        Object colValue = rs.getObject(colName);
                        extData.getMapColumnMeta().get(colName).getClazz().cast(colValue);
                        valsMap.put(colName, colValue);
                    }
                    builder.build(source.destinationConfig, extData.getMapColumnMeta(), valsMap, reporter);
                    cur++;

                }
                rs.close();
                stm.close();

            } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                reporter.reportError(e.getMessage(), e);
                System.exit(1);
            }
        } else {
            reporter.reportError("SQL is blank in the source!");
            System.exit(1);
        }
        try {
            srcCon.close();
        } catch (SQLException e) {
            reporter.reportError("Error in closing connection! " + e.getMessage(), e);
        }

        return extData;
    }
}
