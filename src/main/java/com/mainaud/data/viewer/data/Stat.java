package com.mainaud.data.viewer.data;

public class Stat {
    private String variable;
    private long count;
    private double average;

    public Stat() {
    }

    public Stat(String variable, long count, double average) {
        this.variable = variable;
        this.count = count;
        this.average = average;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }
}
