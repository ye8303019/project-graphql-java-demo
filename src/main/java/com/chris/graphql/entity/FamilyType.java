package com.chris.graphql.entity;

/**
 * Created by ye830 on 12/8/2017.
 */
public enum FamilyType {
    ORIGINAL("ORIGINAL"),
    INPADOC("INPADOC");

    private String familyType;

    private FamilyType(String familyType){
        this.familyType = familyType;

    }

    public String getFamilyType() {
        return familyType;
    }

    public void setFamilyType(String familyType) {
        this.familyType = familyType;
    }
}
