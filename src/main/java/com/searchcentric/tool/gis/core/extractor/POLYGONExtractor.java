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
public class POLYGONExtractor extends Extractor{
    @Override
    public ExtractedData extract(SourceConfig source, Reporter reporter) {
        ExtractedData extData = new ExtractedData();
        ConnectionFactory cf = new ConnectionFactory(reporter);
        Connection srcCon = cf.getConnection(source.connection.jdbc_driver, source.connection.jdbc_path);

        // perform query to extract main data
        // some magic for extraction, just full extraction
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

            // get the uniq groups of the gis data
            try {
                sql = constructUniqFieldSql(mainSql, source.query);
                reporter.reportInfo("Uniq sql - " + sql);
                Statement stm = srcCon.createStatement();
                ResultSet rs = stm.executeQuery(sql);
                while (rs.next()) {
                    String fieldValue = rs.getObject(source.query.groups.field_alias).toString();
                    if (extData.getMapData().get(fieldValue) == null) {
                        extData.getMapData().put(fieldValue, new HashMap<String,List>());
                    }

                }
                rs.close();
                stm.close();
            } catch (SQLException e) {
                reporter.reportError(e.getMessage(), e);
                System.exit(1);
            }
            // perform query for each grouping field
            try {
                Builder builder= (Builder) Class.forName("com.searchcentric.tool.gis.core.builder." + source.destinationConfig.type + "Builder").newInstance();
                builder.init(source.destinationConfig, reporter);
                int fieldSize = extData.getMapData().keySet().size();
                int cur = 1;
                for (String field : extData.getMapData().keySet()) {
                    reporter.reportInfo("Requesting GIS for: " + field + ". Item " + cur + " of " + fieldSize);
                    sql = constructMainSql(mainSql, source.query, field);

                    Statement stm = srcCon.createStatement();
                    ResultSet rs = stm.executeQuery(sql);
                    Map<String, List> valsMap = new HashMap<String, List>();
                    while (rs.next()) {
                        for (String colName : extData.getMapColumnMeta().keySet()) {
                            Object colValue = rs.getObject(colName);
                            extData.getMapColumnMeta().get(colName).getClazz().cast(colValue);

                            List valColList = valsMap.get(colName);
                            if (valColList == null) {
                                valColList = new ArrayList();
                                valsMap.put(colName, valColList);
                            }
                            valColList.add(colValue);
                        }
                    }
                    rs.close();
                    stm.close();
                    cur++;
//                    reporter.reportInfo(valsMap.toString());
                    builder.build(source.destinationConfig, extData.getMapColumnMeta(), valsMap, reporter);

                }
            } catch (SQLException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
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

    public String constructUniqFieldSql(String sql, QueryConfig query) {
        StringBuilder sb = new StringBuilder();
        sb.append("select distinct ").append(query.groups.field_alias);
        sb.append(" from (").append(sql).append(") dt");

        return sb.toString();
    }

    public String constructMainSql(String sql, QueryConfig query, String fieldValue) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(sql);
        sb.append(" and ").append(query.groups.field).append(" = '").append(fieldValue).append("' ");
        sb.append("order by ").append(query.groups.order);

        return sb.toString();
    }
}
