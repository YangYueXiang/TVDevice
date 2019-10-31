package com.boe.tvdevice.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Bean {

    /**
     * data : [{"default":"0","filepath":"http://10.251.159.9:8080/image/program/20190821102927T45/显示器件首页.png","name":"显示器件首页.png","type":"1","programId":107},{"photosets":[{"filepath":"http://10.251.159.9:8080/image/program/photoset/20190827025546T21/00.jpg","name":"00","photosetId":61},{"filepath":"http://10.251.159.9:8080/image/program/photoset/20190827025540T25/u=1625258204,950365741&fm=15&gp=0.jpg","name":"u=1625258204,950365741&fm=15&gp=0","photosetId":60},{"filepath":"http://10.251.159.9:8080/image/program/photoset/20190827025531T05/6.jpg","name":"6","photosetId":59},{"filepath":"http://10.251.159.9:8080/image/program/photoset/20190827025401T57/770.jpg","name":"770","photosetId":58}],"default":"0","filepath":null,"planInterval":5,"name":"最美四种花","type":"3","programId":151},{"default":"0","filepath":"http://10.251.159.9:8080/image/program/20190827033652T40/屏下指纹识别0122.mp4","name":"屏下指纹识别0122(1)","type":"2","programId":235,"coverName":"http://10.251.159.9:8080/image/default.jpg"},{"photosets":[{"filepath":"http://10.251.159.9:8080/image/program/photoset/20190903062048T13/Desert.jpg","name":"Desert","photosetId":107},{"filepath":"http://10.251.159.9:8080/image/program/photoset/20190903062037T28/Koala.jpg","name":"Koala","photosetId":106},{"filepath":"http://10.251.159.9:8080/image/program/photoset/20190830085344T66/70.jpg","name":"70","photosetId":105}],"default":"0","filepath":null,"planInterval":5,"name":"图片集测试1","type":"3","programId":256}]
     * sum : 947729
     * state : s
     * message :
     */

    private int sum;
    private String state;
    private String message;
    private List<DataBean> data;

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * default : 0
         * filepath : http://10.251.159.9:8080/image/program/20190821102927T45/显示器件首页.png
         * name : 显示器件首页.png
         * type : 1
         * programId : 107
         * photosets : [{"filepath":"http://10.251.159.9:8080/image/program/photoset/20190827025546T21/00.jpg","name":"00","photosetId":61},{"filepath":"http://10.251.159.9:8080/image/program/photoset/20190827025540T25/u=1625258204,950365741&fm=15&gp=0.jpg","name":"u=1625258204,950365741&fm=15&gp=0","photosetId":60},{"filepath":"http://10.251.159.9:8080/image/program/photoset/20190827025531T05/6.jpg","name":"6","photosetId":59},{"filepath":"http://10.251.159.9:8080/image/program/photoset/20190827025401T57/770.jpg","name":"770","photosetId":58}]
         * planInterval : 5
         * coverName : http://10.251.159.9:8080/image/default.jpg
         */

        @SerializedName("default")
        private String defaultX;
        private String filepath;
        private String name;
        private String type;
        private int programId;
        private int planInterval;
        private String coverName;
        private List<PhotosetsBean> photosets;
        private String coverId;

        public String getCoverId() {
            return coverId;
        }

        public void setCoverId(String coverId) {
            this.coverId = coverId;
        }

        public String getDefaultX() {
            return defaultX;
        }

        public void setDefaultX(String defaultX) {
            this.defaultX = defaultX;
        }

        public String getFilepath() {
            return filepath;
        }

        public void setFilepath(String filepath) {
            this.filepath = filepath;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getProgramId() {
            return programId;
        }

        public void setProgramId(int programId) {
            this.programId = programId;
        }

        public int getPlanInterval() {
            return planInterval;
        }

        public void setPlanInterval(int planInterval) {
            this.planInterval = planInterval;
        }

        public String getCoverName() {
            return coverName;
        }

        public void setCoverName(String coverName) {
            this.coverName = coverName;
        }

        public List<PhotosetsBean> getPhotosets() {
            return photosets;
        }

        public void setPhotosets(List<PhotosetsBean> photosets) {
            this.photosets = photosets;
        }

        public static class PhotosetsBean {
            /**
             * filepath : http://10.251.159.9:8080/image/program/photoset/20190827025546T21/00.jpg
             * name : 00
             * photosetId : 61
             */

            private String filepath;
            private String name;
            private int photosetId;

            public String getFilepath() {
                return filepath;
            }

            public void setFilepath(String filepath) {
                this.filepath = filepath;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getPhotosetId() {
                return photosetId;
            }

            public void setPhotosetId(int photosetId) {
                this.photosetId = photosetId;
            }
        }
    }
}
