package com.rcplatform.phototalk.bean;

public class ServiceSimpleNotice {

    public String state;

    public String id;

    public String type;

    public ServiceSimpleNotice(String state, String id, String type) {
        super();
        this.state = state;
        this.id = id;
        this.type = type;
    }

    public ServiceSimpleNotice() {
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
