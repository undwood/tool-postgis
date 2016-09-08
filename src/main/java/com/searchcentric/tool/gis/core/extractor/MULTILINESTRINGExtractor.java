package com.searchcentric.tool.gis.core.extractor;

import com.kadme.tool.log.Reporter;
import com.searchcentric.tool.gis.model.config.QueryConfig;
import com.searchcentric.tool.gis.model.config.SourceConfig;
import com.searchcentric.tool.gis.model.data.ExtractedData;
import com.searchcentric.tool.gis.util.ConnectionFactory;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by undwood on 11.08.16.
 */
public class MULTILINESTRINGExtractor extends Extractor {

    @Override
    public ExtractedData extract(SourceConfig source, Reporter reporter) {
        ExtractedData extData = new ExtractedData();
        ConnectionFactory cf = new ConnectionFactory(reporter);
        Connection srcCon = cf.getConnection(source.connection.jdbc_driver, source.connection.jdbc_path);

        // perform query to extract main data
        // some magic for extraction, just full extraction
        QueryConfig query = source.query;
        String sql = "";
        try {
            if (StringUtils.isNotBlank(query.sql_inline)) {
                sql = query.sql_inline;
            } else if (StringUtils.isNotBlank(query.sql)) {
                sql = new String(Files.readAllBytes(Paths.get(query.sql))) + query.limit_clause;
            } else {
                reporter.reportError("No SQL for source!");
                System.exit(1);
            }
        } catch (IOException e) {
            reporter.reportError(e.getMessage(), e);
            System.exit(1);
        }

        if(StringUtils.isNotBlank(sql)) {

            //get metadata from the query
            try {
                Statement stm = srcCon.createStatement();
                ResultSet rs = stm.executeQuery(sql);
                extData.setMapColumnMeta(getColumnsMeta(rs.getMetaData()));
                rs.close();
                stm.close();
            }catch (SQLException | ClassNotFoundException e) {
                reporter.reportError(e.getMessage(), e);
                System.exit(1);
            }

            // get the uniq groups of the gis data
            try {
                sql = constructUniqFieldSql(source.query);
                Statement stm = srcCon.createStatement();
                ResultSet rs = stm.executeQuery(sql);
                while(rs.next()) {

                    String fieldValue = rs.getObject(source.query.groups.field_alias).toString();
                    String subFieldValue = rs.getObject(source.query.groups.sub_field_alias).toString();
                    Map subList = (Map)extData.getMapData().get(fieldValue);
                    if(subList == null) {
                        subList = new HashMap<String, Object>();
                        extData.getMapData().put(fieldValue, subList);
                    }
                    Object obj = subList.get(subFieldValue);
                    if(obj == null) {
                        obj = new Object();
                        subList.put(subFieldValue, obj);
                    }
                }
                rs.close();
                stm.close();
            }catch (SQLException e) {
                reporter.reportError(e.getMessage(), e);
                System.exit(1);
            }
            // perform query for each grouping field
            try{
                int fieldSize = extData.getMapData().keySet().size();
                int cur = 1;
                for(String field : extData.getMapData().keySet()) {
                    reporter.reportInfo("Requesting GIS for: " + field + ". Item " + cur + " of " + fieldSize);
                    sql = constructMainSql(source.query, field);
                    Statement stm = srcCon.createStatement();
                    ResultSet rs = stm.executeQuery(sql);
                    while(rs.next()) {
                        String subFieldValue = rs.getObject(source.query.groups.sub_field_alias).toString();
                        for(String colName : extData.getMapColumnMeta().keySet()) {
                            Object colValue = rs.getObject(colName);
                            extData.getMapColumnMeta().get(colName).getClazz().cast(colValue);
                            Object valsMap = ((Map)extData.getMapData().get(field)).get(subFieldValue);
                            if(!(valsMap instanceof Map)) {
                                ((Map)extData.getMapData().get(field)).remove(subFieldValue);
                                valsMap = null;
                            }
                            if(valsMap == null) {
                                valsMap = new HashMap<String, List>();
                                ((Map)extData.getMapData().get(field)).put(subFieldValue, valsMap);
                            }
//                            Map.class.cast(valsMap);
                            List valColList = (List)((Map)valsMap).get(colName);
                            if(valColList == null) {
                                valColList = new ArrayList();
                                ((Map)valsMap).put(colName, valColList);
                            }
                            valColList.add(colValue);
                        }
                    }
                    rs.close();
                    stm.close();
                }
            } catch (SQLException | IOException e) {
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

    public String constructUniqFieldSql(QueryConfig query) {
        StringBuilder sb = new StringBuilder();
        sb.append("select distinct ").append(query.groups.field).append(" as ").append(query.groups.field_alias);
        sb.append(", ").append(query.groups.sub_field).append(" as  ").append(query.groups.sub_field_alias);
        sb.append(" from ").append(query.table).append(" group by ").append(query.groups.field);
        sb.append(", ").append(query.groups.sub_field);

        return sb.toString();
    }

    public String constructMainSql(QueryConfig query, String fieldValue) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(new String(Files.readAllBytes(Paths.get(query.sql))));
        sb.append(" and ").append(query.groups.field).append(" = '").append(fieldValue).append("' ");
        sb.append("order by ").append(query.groups.sub_field).append(", ").append(query.groups.order);

        return sb.toString();
    }

}
