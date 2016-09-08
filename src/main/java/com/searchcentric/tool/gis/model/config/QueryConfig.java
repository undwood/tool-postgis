package com.searchcentric.tool.gis.model.config;

/**
 * Created by undwood on 11.08.16.
 */
public class QueryConfig {
    public String sql;
    public String sql_inline;
    public String table;

    public String limit_clause;
    public GroupsConfig groups;

    public String getSql() {
        return sql;
    }

    public String getLimit_clause() {
        return limit_clause;
    }

    public void setLimit_clause(String limit_clause) {
        this.limit_clause = limit_clause;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSql_inline() {
        return sql_inline;
    }

    public void setSql_inline(String sql_inline) {
        this.sql_inline = sql_inline;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }


    public GroupsConfig getGroups() {
        return groups;
    }

    public void setGroups(GroupsConfig groups) {
        this.groups = groups;
    }
}
