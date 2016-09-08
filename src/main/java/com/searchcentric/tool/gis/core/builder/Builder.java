package com.searchcentric.tool.gis.core.builder;

import com.kadme.tool.log.Reporter;
import com.searchcentric.tool.gis.model.config.ColumnConfig;
import com.searchcentric.tool.gis.model.config.DestinationConfig;
import com.searchcentric.tool.gis.model.config.SourceConfig;
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
import java.util.Map;

/**
 * Created by undwood on 30.08.16.
 */
public abstract class Builder {

    public static String SEQ_CREATE = "CREATE SEQUENCE #SEQ# INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1";
    public static String SEQ_CHECK = "SELECT count(*) FROM information_schema.sequences where sequence_name = '#SEQ#' and sequence_schema = '#SCHEMA#'";
    public static String TBL_CHECK = "SELECT count(*) FROM information_schema.tables where table_type = 'BASE TABLE' and table_name = '#TABLE#' and table_schema = '#SCHEMA#'";
    public static String TBL_TRUNCATE = "TRUNCATE TABLE #TBL#";
    public static String DROP_GEOM = "SELECT DropGeometryColumn ('#SCHEMA#','#TABLE#','#COLUMN#')";
    public static String ADD_GEOM = "SELECT AddGeometryColumn ('#SCHEMA#','#TABLE#','#COLUMN#',#SRID#,'#TYPE#', 2)";

    public  abstract Boolean build(DestinationConfig config, Map<String, ColumnMeta> mapColumnMeta, Object mapData, Reporter reporter);

    public void init(DestinationConfig dest, Reporter reporter) {
        ConnectionFactory cf = new ConnectionFactory(reporter);
        Connection dstCon = cf.getConnection(dest.connection.jdbc_driver, dest.connection.jdbc_path);
        // create sequence if not exists
        reporter.reportInfo("Checking sequence for existing - " + dest.sequence);
        Boolean createSeq = false;
        try {

            String[] seq = dest.sequence.split("[.]");
            String seqName = "";
            String schemaName = "public";
            if(seq.length > 1) {
                seqName = seq[1];
                schemaName = seq[0];
            } else {
                seqName = seq[0];
            }

            Statement stm = dstCon.createStatement();
            ResultSet rs = stm.executeQuery(SEQ_CHECK.replace("#SEQ#", seqName).replace("#SCHEMA#", schemaName));
            if(rs.next()) {
                createSeq = rs.getInt(1) == 0;
            }
            rs.close();
            stm.close();
        } catch (SQLException e) {
            reporter.reportError(e.getMessage(), e);
            System.exit(1);
        }
        if(createSeq) {
            reporter.reportInfo("Creating sequence - " + dest.sequence);
            try {
                Statement stm = dstCon.createStatement();
                stm.execute(SEQ_CREATE.replace("#SEQ#", dest.sequence));
                stm.close();
            } catch (SQLException e) {
                reporter.reportError(e.getMessage(), e);
                System.exit(1);
            }
        }
        // create table if not exists

        String sql = "";


        // getting the correct name of the table - including schema name
        String[] scTb = dest.destination_table.name.split("[.]");
        String tblName = "";
        String schemaName = "public";
        if(scTb.length > 1) {
            tblName = scTb[1];
            schemaName = scTb[0];
        } else {
            tblName = scTb[0];
        }


        // check existence of the table
        reporter.reportInfo("Check existence of the table - " + dest.destination_table.name);
        Boolean createTable = false;
        try {
            sql = TBL_CHECK.replace("#TABLE#", tblName).replace("#SCHEMA#",schemaName);
            Statement stm = dstCon.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            if(rs.next()) {
                createTable = rs.getInt(1) == 0;
            }
            rs.close();
            stm.close();
        } catch (SQLException e) {
            reporter.reportError(e.getMessage(), e);
            System.exit(1);
        }

        if(createTable) {
            try {

                if (StringUtils.isNotBlank(dest.destination_table.create_inline)) {
                    sql = dest.destination_table.create_inline;
                } else if (StringUtils.isNotBlank(dest.destination_table.create)) {
                    sql = new String(Files.readAllBytes(Paths.get(dest.destination_table.create)));
                } else {
                    reporter.reportError("No SQL for destination table!");
                    System.exit(1);
                }
                if(StringUtils.isNotBlank(sql)) {
                    reporter.reportInfo("Creating the table - " + dest.destination_table.name);
                    sql = sql.replace("#SEQ#", dest.sequence);
                    reporter.reportInfo("Create query - " + sql);
                    Statement stm = dstCon.createStatement();
                    reporter.reportInfo("Created " + stm.execute(sql));
                    stm.close();
                }
            } catch (SQLException | IOException e) {
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

}
