package com.chris.graphql.entity;

/**
 * Created by ye830 on 12/2/2017.
 */
public class PatentBiblio extends Patent {
    private String apno;
    private Person ans;
    private FamilyType familyType;

    public String getApno() {
        return apno;
    }

    public void setApno(String apno) {
        this.apno = apno;
    }

    public Person getAns() {
        return ans;
    }

    public void setAns(Person ans) {
        this.ans = ans;
    }

    public FamilyType getFamilyType() {
        return familyType;
    }

    public void setFamilyType(FamilyType familyType) {
        this.familyType = familyType;
    }
}
