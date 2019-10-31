package com.boe.tvdevice.bean;

public class PhotosetInfoBean {

    /**
     * playInterval : 3
     * photosetIds : 224,226,228,225,227,
     * state : s
     * message :
     */

    private int playInterval;
    private String photosetIds;
    private String state;
    private String message;

    public int getPlayInterval() {
        return playInterval;
    }

    public void setPlayInterval(int playInterval) {
        this.playInterval = playInterval;
    }

    public String getPhotosetIds() {
        return photosetIds;
    }

    public void setPhotosetIds(String photosetIds) {
        this.photosetIds = photosetIds;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
