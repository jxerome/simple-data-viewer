package com.mainaud.data.viewer.data;

import java.util.List;

public class Stats {
    private List<Stat> lines;
    private Stat others;

    public List<Stat> getLines() {
        return lines;
    }

    public void setLines(List<Stat> lines) {
        this.lines = lines;
    }

    public Stat getOthers() {
        return others;
    }

    public void setOthers(Stat others) {
        this.others = others;
    }
}
