package com.boe.tvdevice.okgo;

public class HttpCommon {
    public static final String baseUrl = "http://10.251.159.9:8080";
    public static final String baseUrl1 = "http://112.126.123.94:8481";
    public static final String baseUrl2 = "http://218.104.69.90:8080";

    //获取图片视频数据
    public static final String getData = baseUrl1 + "/program/getProgramInfo";

    //删除通知后台
    public static final String tellDelete=baseUrl1+"/program/untyingProgram";

    //获取时间
    public static final String getDurationTime=baseUrl1+"/program/getProgramPlayInterval";// programId
}
