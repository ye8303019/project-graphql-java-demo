package com.chris.graphql.entity;

import java.util.List;

/**
 * Created by ye830 on 12/8/2017.
 */
public class Legal {
    private String l001ep;
    private List<String> legalStatus;
    private List<String> eventStatus;

    public String getL001ep() {
        return l001ep;
    }

    public void setL001ep(String l001ep) {
        this.l001ep = l001ep;
    }

    public List<String> getLegalStatus() {
        return legalStatus;
    }

    public void setLegalStatus(List<String> legalStatus) {
        this.legalStatus = legalStatus;
    }

    public List<String> getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(List<String> eventStatus) {
        this.eventStatus = eventStatus;
    }
}
