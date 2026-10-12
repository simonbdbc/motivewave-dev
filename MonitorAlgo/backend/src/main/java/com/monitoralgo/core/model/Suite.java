package com.monitoralgo.core.model;

public class Suite {
    private Integer id;
    private String pattern;

    public Suite() {
    }

    public Suite(Integer id, String pattern) {
        this.id = id;
        this.pattern = pattern;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
