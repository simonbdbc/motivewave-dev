package com.monitoralgo.core.model;

public class Pattern {
    private Integer id;
    private Suite suiteRenko;
    private Suite suiteHilo1;
    private Suite suiteHilo2;

    public Pattern() {
    }

    public Pattern(Integer id, Suite suiteRenko, Suite suiteHilo1, Suite suiteHilo2) {
        this.id = id;
        this.suiteRenko = suiteRenko;
        this.suiteHilo1 = suiteHilo1;
        this.suiteHilo2 = suiteHilo2;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Suite getSuiteRenko() {
        return suiteRenko;
    }

    public void setSuiteRenko(Suite suiteRenko) {
        this.suiteRenko = suiteRenko;
    }

    public Suite getSuiteHilo1() {
        return suiteHilo1;
    }

    public void setSuiteHilo1(Suite suiteHilo1) {
        this.suiteHilo1 = suiteHilo1;
    }

    public Suite getSuiteHilo2() {
        return suiteHilo2;
    }

    public void setSuiteHilo2(Suite suiteHilo2) {
        this.suiteHilo2 = suiteHilo2;
    }
}
