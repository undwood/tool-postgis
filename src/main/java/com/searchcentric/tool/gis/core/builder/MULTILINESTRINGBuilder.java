package com.searchcentric.tool.gis.core.builder;

import com.kadme.tool.log.Reporter;
import com.searchcentric.tool.gis.model.config.ColumnConfig;
import com.searchcentric.tool.gis.model.config.DestinationConfig;
import com.searchcentric.tool.gis.model.config.SourceConfig;
import com.searchcentric.tool.gis.model.data.ColumnMeta;
import com.searchcentric.tool.gis.model.data.ExtractedData;
import com.searchcentric.tool.gis.util.ConnectionFactory;
import org.apache.commons.lang.StringUtils;
import org.postgis.LineString;
import org.postgis.MultiLineString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by undwood on 30.08.16.
 */
public class MULTILINESTRINGBuilder extends Builder {

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


        List<String> insertList = new ArrayList<>();

//        for(String multiLine : extData.getMapData().keySet()) {
            String ml = "ST_GeomFromText('MULTILINESTRING(";
            Map<String, Set> dataSet = new HashMap<>();
            Map<String, Object> mlData = (Map)mapData;
            for(String line: mlData.keySet()) {
                Map<String, Object> data = (Map)(mlData.get(line));
                for(ColumnConfig ccfg : dest.destination_table.columns) {
                    if(!ccfg.is_geometry) {
                        List colData = (List)data.get(ccfg.mapping);
                        for(int j = 0; j < colData.size(); j++) {
                            Set s = dataSet.get(ccfg.name);
                            if(s == null) {
//                                Class cl = extData.getMapColumnMeta().get(ccfg.mapping).getClazz();
                                s = new HashSet<>();
                                dataSet.put(ccfg.name, s);
                            }
                            s.add(colData.get(j));
                        }

                    } else {
                        String lineTxt = "(";
                        List<Double> lon = (List<Double>)data.get(dest.lon_y);
                        List<Double> lat = (List<Double>)data.get(dest.lat_x);
                        if(lon.size() != lat.size()) {
                            reporter.reportError("Coordinate lists have different sizes!!");
                            System.exit(1);
                        }
                        for(int i = 0; i < lon.size(); i++) {
                            Double lonVal = lon.get(i);
                            Double latVal = lat.get(i);
                            lineTxt += latVal;
                            lineTxt += " ";
                            lineTxt += lonVal;
                            lineTxt += ",";

                        }

                        lineTxt = lineTxt.substring(0, lineTxt.length() - 1) + ")";
                        ml += lineTxt;
                        ml += ",";
                    }
                }

// MULTILINESTRING( (0 0,1 1,1 2) , (2 3,3 2,5 4))

            }
            ml = ml.substring(0, ml.length() - 1) + ")',"+ dest.srid+")";
            String newInsert = new String(sql);
            for(ColumnConfig ccfg : dest.destination_table.columns) {
                if(ccfg.is_geometry) {
                    newInsert = newInsert.replace("#" + ccfg.name + "#", ml);
                } else {
                    try {
                        newInsert = newInsert.replace("#" + ccfg.name + "#", buildValue(mapColumnMeta, dataSet.get(ccfg.mapping), ccfg.mapping));
                    } catch (IllegalAccessException | InstantiationException e) {
                        reporter.reportError("Error in creating insert query - " + e.getMessage(), e);
                    }
                }
            }
            insertList.add(newInsert);
//        }
        // inserting data
        try {
            Statement stm = dstCon.createStatement();
            for(String insSql : insertList) {
                stm.addBatch(insSql);
            }
            stm.executeBatch();
            stm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        try {
            dstCon.close();
        } catch (SQLException e) {
            reporter.reportError("Error in closing connection! " + e.getMessage(), e);
        }
        return true;
    }



    @Override
    public void init(DestinationConfig dest, Reporter reporter) {
        ConnectionFactory cf = new ConnectionFactory(reporter);
        Connection dstCon = cf.getConnection(dest.connection.jdbc_driver, dest.connection.jdbc_path);
        // create sequence if not exists
        reporter.reportInfo("Creating sequence if not exists - " + dest.sequence);
        try {
            Statement stm = dstCon.createStatement();
            stm.execute(SEQ_CREATE.replace("#SEQ#", dest.sequence));
            stm.close();
        } catch (SQLException e) {
            reporter.reportError(e.getMessage(), e);
            System.exit(1);
        }
        // create table if not exists
        reporter.reportInfo("Creating table if not exists - " + dest.destination_table.name);
        String sql = "";
        try {
            if (StringUtils.isNotBlank(dest.destination_table.create_inline)) {
                sql = dest.destination_table.create_inline;
            } else if (StringUtils.isNotBlank(dest.destination_table.create)) {
                sql = new String(Files.readAllBytes(Paths.get(dest.destination_table.create)));
            } else {
                reporter.reportError("No SQL for destination table!");
                System.exit(1);
            }
        } catch (IOException e) {
            reporter.reportError(e.getMessage(), e);
            System.exit(1);
        }
        if(StringUtils.isNotBlank(sql)) {
            try {
                sql = sql.replace("#SEQ#", dest.sequence);
                Statement stm = dstCon.createStatement();
                stm.execute(sql);
                stm.close();
            } catch (SQLException e) {
                reporter.reportError(e.getMessage(), e);
                System.exit(1);
            }
        }
        // truncate data
        reporter.reportInfo("Truncating data in the " + dest.destination_table.name);
        sql = TBL_TRUNCATE.replace("#TBL#", dest.destination_table.name);
        try {
            Statement stm = dstCon.createStatement();
            stm.execute(sql);
            stm.close();
        } catch (SQLException e) {
            reporter.reportError(e.getMessage(), e);
            System.exit(1);
        }
        // drop old geometry column
        reporter.reportInfo("Dropping old geometry column ");
        String[] scTb = dest.destination_table.name.split("[.]");
        String tblName = "";
        String schemaName = "public";
        if(scTb.length > 1) {
            tblName = scTb[1];
            schemaName = scTb[0];
        } else {
            tblName = scTb[0];
        }
        String geoColumn = "";

        for(ColumnConfig ccfg : dest.destination_table.columns) {
            if(ccfg.is_geometry) {
                geoColumn = ccfg.name;
                break;
            }
        }
        if(StringUtils.isNotBlank(geoColumn)) {
            sql = DROP_GEOM.replace("#SCHEMA#", schemaName).replace("#TABLE#", tblName).replace("#COLUMN#", geoColumn);
            try {
                Statement stm = dstCon.createStatement();
                stm.execute(sql);
                stm.close();
            } catch (SQLException e) {
                reporter.reportWarning(e.getMessage());
//                System.exit(1);
            }
        }
        // add geometry column
        reporter.reportInfo("Adding geometry column " + geoColumn);
        sql = ADD_GEOM.replace("#SCHEMA#", schemaName).replace("#TABLE#", tblName).replace("#COLUMN#", geoColumn).
                replace("#SRID#",dest.getSrid()).replace("#TYPE#", dest.type);
        try {
            Statement stm = dstCon.createStatement();
            stm.execute(sql);
            stm.close();
        } catch (SQLException e) {
            reporter.reportError(e.getMessage(), e);
            System.exit(1);
        }

        try {
            dstCon.close();
        } catch (SQLException e) {
            reporter.reportError("Error in closing connection! " + e.getMessage(), e);
        }
    }

    private String buildValue(Map<String, ColumnMeta> mapColumnMeta, Set valueSet, String colName) throws IllegalAccessException, InstantiationException {
        String retValue = "";
        Class cl = mapColumnMeta.get(colName).getClazz();
        if(cl.newInstance() instanceof Number) {
            for(Object val : valueSet) {
                retValue += val.toString();
                break;
            }
        } else if (cl.newInstance() instanceof String) {
            if(valueSet.isEmpty()) {
                retValue += "''";
            } else {
                retValue += "'";

                for (Object val : valueSet) {
                    if(val != null) {
                        retValue += val.toString();
                        retValue += ",";
                    }
                }
                if(retValue.length() > 1) {
                    retValue = retValue.substring(0, retValue.length() - 1);
                }
                retValue += "'";
            }
        } else if (cl.newInstance() instanceof Date) {
            for(Object val : valueSet) {
                Date ddd = (Date) val;
                SimpleDateFormat sdf = new SimpleDateFormat("DD-MM-YYYY");
                retValue = "to_date('" + sdf.format(ddd) + "','DD-MM-YYYY')";
                break;
            }
        }
        return retValue;

    }
}
