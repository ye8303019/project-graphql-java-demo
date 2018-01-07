package com.chris.graphql.entity;

/**
 * Created by ye830 on 12/2/2017.
 */
public class PatentBiblioAndLegal {
    private PatentBiblio patentBiblio;
    private Legal legal;

    public PatentBiblio getPatentBiblio() {
        return patentBiblio;
    }

    public void setPatentBiblio(PatentBiblio patentBiblio) {
        this.patentBiblio = patentBiblio;
    }

    public Legal getLegal() {
        return legal;
    }

    public void setLegal(Legal legal) {
        this.legal = legal;
    }
}
