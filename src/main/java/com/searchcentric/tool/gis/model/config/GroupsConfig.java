package com.searchcentric.tool.gis.model.config;

/**
 * Created by undwood on 11.08.16.
 */
public class GroupsConfig {
    public String field;
    public String field_alias;
    public String sub_field;
    public String sub_field_alias;
    public String inner;
    public String inner_alias;
    public String outer;
    public String outer_alias;
    public String order;
    public String order_alias;

    public String getField_alias() {
        return field_alias;
    }

    public void setField_alias(String field_alias) {
        this.field_alias = field_alias;
    }

    public String getSub_field_alias() {
        return sub_field_alias;
    }

    public void setSub_field_alias(String sub_field_alias) {
        this.sub_field_alias = sub_field_alias;
    }

    public String getInner_alias() {
        return inner_alias;
    }

    public void setInner_alias(String inner_alias) {
        this.inner_alias = inner_alias;
    }

    public String getOuter_alias() {
        return outer_alias;
    }

    public void setOuter_alias(String outer_alias) {
        this.outer_alias = outer_alias;
    }

    public String getOrder_alias() {
        return order_alias;
    }

    public void setOrder_alias(String order_alias) {
        this.order_alias = order_alias;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getSub_field() {
        return sub_field;
    }

    public void setSub_field(String sub_field) {
        this.sub_field = sub_field;
    }

    public String getInner() {
        return inner;
    }

    public void setInner(String inner) {
        this.inner = inner;
    }

    public String getOuter() {
        return outer;
    }

    public void setOuter(String outer) {
        this.outer = outer;
    }
}
