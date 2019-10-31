package com.boe.tvdevice.bean;

import java.io.Serializable;

public class SpSaveDefault implements Serializable {
    private int programId;
    private String type;
    private String houzhuiname;
    private String name;
    private String coverId;
    private String videocoverurl;
    private String defaultX;

    public String getDefaultX() {
        return defaultX;
    }

    public void setDefaultX(String defaultX) {
        this.defaultX = defaultX;
    }

    public String getVideocoverurl() {
        return videocoverurl;
    }

    public void setVideocoverurl(String videocoverurl) {
        this.videocoverurl = videocoverurl;
    }

    public String getCoverId() {
        return coverId;
    }

    public void setCoverId(String coverId) {
        this.coverId = coverId;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHouzhuiname() {
        return houzhuiname;
    }

    public void setHouzhuiname(String houzhuiname) {
        this.houzhuiname = houzhuiname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
