package com.searchcentric.tool.gis.core.builder;

import com.kadme.tool.log.Reporter;
import com.searchcentric.tool.gis.model.config.ColumnConfig;
import com.searchcentric.tool.gis.model.config.DestinationConfig;
import com.searchcentric.tool.gis.model.data.ColumnMeta;
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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by undwood on 30.08.16.
 */
public class POINTBuilder extends Builder {


    @Override
    public Boolean build(DestinationConfig dest, Map<String, ColumnMeta> mapColumnMeta, Object mapData, Reporter reporter) {
        ConnectionFactory cf = new ConnectionFactory(reporter);
        Connection dstCon = cf.getConnection(dest.connection.jdbc_driver, dest.connection.jdbc_path);
        // load data
        reporter.reportInfo("Start loading data...");
        // build mapping
        Map<String, String> mapping = new HashMap<>();
        String sql = "INSERT INTO " + dest.destination_table.name + "(";

        for(ColumnConfig ccfg : dest.destination_table.columns) {
            mapping.put(ccfg.name, ccfg.mapping);
            sql += ccfg.name;
            sql += ",";
        }
        // replace last ',' to ')'
        sql = sql.substring(0, sql.length() - 1) + ")";
        sql += " VALUES (";

        for(ColumnConfig ccfg : dest.destination_table.columns) {
            if(StringUtils.isNotBlank(ccfg.value)) {
                sql += ccfg.value;
            } else {
                sql += "#";
                sql += ccfg.name;
                sql += "#";
            }
            sql += ",";
        }
        sql = sql.substring(0, sql.length() - 1) + ")";

        String ml = "ST_GeomFromText('POINT(";
        Map<String, Object> dataSet = new HashMap<>();
        Map<String, Object> data = (Map)mapData;
        boolean skip = false;
//        reporter.reportInfo(data.toString());
        for(ColumnConfig ccfg : dest.destination_table.columns) {
            if(!ccfg.is_geometry) {
                Object colData = data.get(ccfg.mapping);
                if(colData != null) {
                    dataSet.put(ccfg.name, colData);
                }
            } else {
                if(data.get(dest.lon_y) == null || data.get(dest.lat_x) == null) {
                    reporter.reportInfo("Skipping... ");
                    skip = true;
                } else {

                    if(!skip) {
                        ml += data.get(dest.lon_y);
                        ml += " ";
                        ml += data.get(dest.lat_x);
                        ml += ")";
                    }
                }
            }
        }
        if(!skip) {
            ml = ml + "'," + dest.srid + ")";
            String newInsert = new String(sql);
            for (ColumnConfig ccfg : dest.destination_table.columns) {
                if (ccfg.is_geometry) {
                    newInsert = newInsert.replace("#" + ccfg.name + "#", ml);
                } else if(StringUtils.isBlank(ccfg.value)){
                    try {
//                        reporter.reportInfo("mapping " + ccfg.mapping);
//                        reporter.reportInfo("name " + ccfg.name);
                        newInsert = newInsert.replace("#" + ccfg.name + "#", buildValue(mapColumnMeta, dataSet.get(ccfg.name), ccfg.mapping));
                    } catch (IllegalAccessException | InstantiationException e) {
                        reporter.reportError("Error in creating insert query - " + e.getMessage(), e);
                    }
                }
            }


            // inserting data
            try {
                Statement stm = dstCon.createStatement();
                reporter.reportTrace("Query: " + newInsert);
                stm.execute(newInsert);
                stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            dstCon.close();
        } catch (SQLException e) {
            reporter.reportError("Error in closing connection! " + e.getMessage(), e);
        }
        return true;
    }


    private String buildValue(Map<String, ColumnMeta> mapColumnMeta, Object value, String colName) throws IllegalAccessException, InstantiationException {
        String retValue = "";
        Class cl = mapColumnMeta.get(colName).getClazz();
        if(cl.newInstance() instanceof Number) {
                retValue += value.toString();
        } else if (cl.newInstance() instanceof String) {
            cl.cast(value);
            if(StringUtils.isBlank((String)value)){
                retValue += "''";
            } else {
                retValue += "'" + value + "'";
            }
        } else if (cl.newInstance() instanceof Date) {
            Date ddd = (Date) value;
            SimpleDateFormat sdf = new SimpleDateFormat("DD-MM-YYYY");
            retValue = "to_date('" + sdf.format(ddd) + "','DD-MM-YYYY')";
        }
        return retValue;

    }
}
