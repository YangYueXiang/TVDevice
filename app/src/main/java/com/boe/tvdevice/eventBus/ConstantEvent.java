package com.boe.tvdevice.eventBus;

public class ConstantEvent {
    private int type;
    private int position;
    private String method;
    private String currentProgramMethod;
    private String seqServer;
    private String failids;

    private String playtype;
    private String playProgramId;
    private String toPlaySeqServer;

    private String ControllerSeqServer;

    private String neicunState;

    public String getNeicunState() {
        return neicunState;
    }

    public void setNeicunState(String neicunState) {
        this.neicunState = neicunState;
    }

    public String getControllerSeqServer() {
        return ControllerSeqServer;
    }

    public void setControllerSeqServer(String controllerSeqServer) {
        ControllerSeqServer = controllerSeqServer;
    }

    public String getToPlaySeqServer() {
        return toPlaySeqServer;
    }

    public void setToPlaySeqServer(String toPlaySeqServer) {
        this.toPlaySeqServer = toPlaySeqServer;
    }

    public String getPlaytype() {
        return playtype;
    }

    public void setPlaytype(String playtype) {
        this.playtype = playtype;
    }

    public String getPlayProgramId() {
        return playProgramId;
    }

    public void setPlayProgramId(String playProgramId) {
        this.playProgramId = playProgramId;
    }

    public String getFailids() {
        return failids;
    }

    public void setFailids(String failids) {
        this.failids = failids;
    }

    public String getCurrentProgramMethod() {
        return currentProgramMethod;
    }

    public void setCurrentProgramMethod(String currentProgramMethod) {
        this.currentProgramMethod = currentProgramMethod;
    }

    public String getSeqServer() {
        return seqServer;
    }

    public void setSeqServer(String seqServer) {
        this.seqServer = seqServer;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public ConstantEvent(int type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getType() {
        return type;
    }

    //下载完成跳转
    public static final int DownLoadOver = 1;
    public static final int NoDefault = 2;
    public static final int DELETEREFRESH = 3;

    //socket长连接用到的

    //给他设备id信息
    public static final int GiveDeviceId = 4;
    public static final int NoticeToDownload = 5;
    public static final int ActivityNoticeToDownload = 7;
    public static final int CloseListShowActivity = 8;
    public static final int Connected = 9;
    public static final int AllDownload = 10;
    public static final int FailDownload = 11;
    public static final int NoticeClose = 11;
    public static final int PicturesAuto = 13;
    public static final int PicturesStopAuto = 14;
    public static final int PicturesLast = 15;
    public static final int PicturesNext = 16;
    public static final int PicturesRestart = 17;
    public static final int VideoPlay = 18;
    public static final int VideoStop = 19;
    public static final int VideoRestart = 20;
}
