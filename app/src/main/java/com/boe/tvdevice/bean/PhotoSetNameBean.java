package com.boe.tvdevice.bean;

import java.util.List;

public class PhotoSetNameBean {
    private String programId;
    private List<DataBean> dataBeans;

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public List<DataBean> getDataBeans() {
        return dataBeans;
    }

    public void setDataBeans(List<DataBean> dataBeans) {
        this.dataBeans = dataBeans;
    }

    public static class DataBean{
        private String photosetId;
        private String name;

        public String getPhotosetId() {
            return photosetId;
        }

        public void setPhotosetId(String photosetId) {
            this.photosetId = photosetId;
        }

        public String getName() {
            return name;
        }

        public  void setName(String name) {
            this.name = name;
        }
    }
}
