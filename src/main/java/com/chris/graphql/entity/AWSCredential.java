package com.chris.graphql.entity;

/**
 * Created by ye830 on 12/22/2017.
 */
public class AWSCredential {
    private String foo;

    private String bar;

    public AWSCredential(String foo, String bar){
        this.foo = foo;
        this.bar = bar;
    }

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }
}
