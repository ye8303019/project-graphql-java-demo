package com.chris.graphql.entity;

import java.util.List;

/**
 * Created by ye830 on 12/2/2017.
 */
public class Patent {
    private String id;
    private String pn;
    private String myName;
    private Person person;
    private List<Patent> citations;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPn() {
        return pn;
    }

    public void setPn(String pn) {
        this.pn = pn;
    }

    public List<Patent> getCitations() {
        return citations;
    }

    public void setCitations(List<Patent> citations) {
        this.citations = citations;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }
}
