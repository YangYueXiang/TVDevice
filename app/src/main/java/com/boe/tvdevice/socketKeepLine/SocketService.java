package com.boe.tvdevice.socketKeepLine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.boe.tvdevice.activity.PictureActivity;
import com.boe.tvdevice.activity.PicturesActivity;
import com.boe.tvdevice.activity.VideoActivity;
import com.boe.tvdevice.bean.SpSaveDefault;
import com.boe.tvdevice.eventBus.ConstantEvent;
import com.boe.tvdevice.utils.ActivityCollector;
import com.boe.tvdevice.utils.SharedPreferencesUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by yzq on 2017/9/26.
 * <p>
 * socket连接服务
 */
public class SocketService extends Service {

    /*socket*/
    private Socket socket;
    /*连接线程*/
    private Thread connectThread;
    private Timer timer = new Timer();
    private OutputStream outputStream;


    private SocketBinder sockerBinder = new SocketBinder();
    private TimerTask task;
    private String houzhuiname;
    private String name;
    private String durationtime;
    String programId;
    String seqServer;
    private SharedPreferences sharedPreferences;


    /*默认重连*/
    private boolean isReConnect = true;

    private Handler handler = new Handler(Looper.getMainLooper());


    @Override
    public IBinder onBind(Intent intent) {
        return sockerBinder;
    }


    public class SocketBinder extends Binder {

        /*返回SocketService 在需要的地方可以通过ServiceConnection获取到SocketService  */
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*初始化socket*/
        initSocket();
        return super.onStartCommand(intent, flags, startId);
    }

    /*初始化socket*/
    private void initSocket() {
        if (socket == null && connectThread == null) {
            connectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    socket = new Socket();
                    try {
                        /*超时时间为2秒*/
                        socket.connect(new InetSocketAddress("112.126.123.94", Integer.valueOf("8638")), 2000);
                        /*连接成功的话  发送心跳包*/
                        if (socket.isConnected()) {
                            /*因为Toast是要运行在主线程的  这里是子线程  所以需要到主线程哪里去显示toast*/
                            Log.i("tcp", "socket已连接");
                            /*发送连接成功的消息*/
                            EventBus.getDefault().postSticky(new ConstantEvent(9));
                            /*发送心跳数据*/
                            sendBeatData();
                            PrintWriter pw = new PrintWriter(socket.getOutputStream());
                            InputStream inputStream = socket.getInputStream();
                            byte[] buffer = new byte[1024];
                            int len = -1;
                            while ((len = inputStream.read(buffer)) != -1) {
                                String data = new String(buffer, 0, len);
                                Log.i("tcp", "收到服务器的数据---------------------------------------------:" + data);
                                if (data.equals("getExhibitsRealId")) {
                                    //发送json给服务器：flag：1，realId:****,method:data;
                                    String serialNumber = android.os.Build.SERIAL;
                                    RealIdBean realIdBean = new RealIdBean();
                                    realIdBean.setFlag("1");
                                    realIdBean.setRealId(serialNumber);
                                    realIdBean.setMethod(data);
                                    Gson gson = new Gson();
                                    String s = gson.toJson(realIdBean);
                                    sendOrder(s);
                                } else if (data.equals("downloadProgram")) {
                                    //通知去下载
                                    // EventBus.getDefault().post(new ConstantEvent(5));
                                    reStartApp();
                                } else if (data.contains("getCurrentProgram")) {
                                    //通知返回当前播放'
                                    SharedPreferencesUtils sharedPreferencesUtils = new SharedPreferencesUtils(getContext());
                                    String currentPlayProgramId = (String) sharedPreferencesUtils.getData("currentPlayProgramId", "");
                                    CurrentProgramBean currentProgramBean = new CurrentProgramBean();
                                    currentProgramBean.setFlag("2");
                                    currentProgramBean.setMethod("getCurrentProgram");
                                    currentProgramBean.setSeqServer(data.substring(data.indexOf(";") + 1));
                                    if (!currentPlayProgramId.equals("")) {
                                        currentProgramBean.setProgramId(currentPlayProgramId);
                                    }
                                    Gson gson = new Gson();
                                    String s = gson.toJson(currentProgramBean);
                                    sendOrder(s);
                                } else if (data.contains("toPlayProgram")) {
                                    String type;
                                    String[] strs = data.split(";");
                                    type = strs[1].toString();
                                    programId = strs[2].toString();
                                    seqServer = strs[3].toString();
                                    SharedPreferencesUtils sharedPreferencesUtils = new SharedPreferencesUtils(getContext());
                                    List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                                    for (int i = 0; i < listdata.size(); i++) {
                                        if (String.valueOf(listdata.get(i).getProgramId()).equals(programId)) {
                                            houzhuiname = listdata.get(i).getHouzhuiname();
                                            name = listdata.get(i).getName();
                                        }
                                    }
                                    if (type.equals("1")) {
                                        ActivityCollector.finishAll();
                                        Intent intent = new Intent(getContext(), PictureActivity.class);
                                        intent.putExtra("programId", programId);
                                        intent.putExtra("houzhuiname", houzhuiname);
                                        intent.putExtra("name", name);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        getContext().startActivity(intent);
                                        playsuccess();
                                    } else if (type.equals("2")) {
                                        ActivityCollector.finishAll();
                                        Intent intent = new Intent(getContext(), VideoActivity.class);
                                        intent.putExtra("programId", programId);
                                        intent.putExtra("name", name);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        getContext().startActivity(intent);
                                        playsuccess();
                                    } else if (type.equals("3")) {
                                        ActivityCollector.finishAll();
                                        Intent intent = new Intent(getContext(), PicturesActivity.class);
                                        intent.putExtra("programId", programId);
                                        intent.putExtra("durationtime", durationtime);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        getContext().startActivity(intent);
                                        playsuccess();
                                    }
                                } else if (data.contains("picturesAuto")) {
                                    ConstantEvent constantEvent = new ConstantEvent(13);
                                    constantEvent.setControllerSeqServer(data.substring(data.indexOf(";") + 1));
                                    EventBus.getDefault().post(constantEvent);
                                    ControllerBean controllerBean = new ControllerBean();
                                    controllerBean.setSeqServer(data.substring(data.indexOf(";") + 1));
                                    controllerBean.setState("s");
                                    controllerBean.setFlag("2");
                                    Gson gson = new Gson();
                                    String s = gson.toJson(controllerBean);
                                    sendOrder(s);
                                } else if (data.contains("picturesStopAuto")) {
                                    ConstantEvent constantEvent = new ConstantEvent(14);
                                    constantEvent.setControllerSeqServer(data.substring(data.indexOf(";") + 1));
                                    EventBus.getDefault().post(constantEvent);
                                    ControllerBean controllerBean = new ControllerBean();
                                    controllerBean.setSeqServer(data.substring(data.indexOf(";") + 1));
                                    controllerBean.setState("s");
                                    controllerBean.setFlag("2");
                                    Gson gson = new Gson();
                                    String s = gson.toJson(controllerBean);
                                    sendOrder(s);
                                } else if (data.contains("picturesLast")) {
                                    ConstantEvent constantEvent = new ConstantEvent(15);
                                    constantEvent.setControllerSeqServer(data.substring(data.indexOf(";") + 1));
                                    EventBus.getDefault().post(constantEvent);
                                    ControllerBean controllerBean = new ControllerBean();
                                    controllerBean.setSeqServer(data.substring(data.indexOf(";") + 1));
                                    controllerBean.setState("s");
                                    controllerBean.setFlag("2");
                                    Gson gson = new Gson();
                                    String s = gson.toJson(controllerBean);
                                    sendOrder(s);
                                } else if (data.contains("picturesNext")) {
                                    ConstantEvent constantEvent = new ConstantEvent(16);
                                    constantEvent.setControllerSeqServer(data.substring(data.indexOf(";") + 1));
                                    EventBus.getDefault().post(constantEvent);
                                    ControllerBean controllerBean = new ControllerBean();
                                    controllerBean.setSeqServer(data.substring(data.indexOf(";") + 1));
                                    controllerBean.setState("s");
                                    controllerBean.setFlag("2");
                                    Gson gson = new Gson();
                                    String s = gson.toJson(controllerBean);
                                    sendOrder(s);
                                } else if (data.contains("picturesRestart")) {
                                    ConstantEvent constantEvent = new ConstantEvent(17);
                                    constantEvent.setControllerSeqServer(data.substring(data.indexOf(";") + 1));
                                    EventBus.getDefault().post(constantEvent);
                                    ControllerBean controllerBean = new ControllerBean();
                                    controllerBean.setSeqServer(data.substring(data.indexOf(";") + 1));
                                    controllerBean.setState("s");
                                    controllerBean.setFlag("2");
                                    Gson gson = new Gson();
                                    String s = gson.toJson(controllerBean);
                                    sendOrder(s);
                                } else if (data.contains("videoPlay")) {
                                    ConstantEvent constantEvent = new ConstantEvent(18);
                                    constantEvent.setControllerSeqServer(data.substring(data.indexOf(";") + 1));
                                    EventBus.getDefault().post(constantEvent);
                                    ControllerBean controllerBean = new ControllerBean();
                                    controllerBean.setSeqServer(data.substring(data.indexOf(";") + 1));
                                    controllerBean.setState("s");
                                    controllerBean.setFlag("2");
                                    Gson gson = new Gson();
                                    String s = gson.toJson(controllerBean);
                                    sendOrder(s);
                                } else if (data.contains("videoStop")) {
                                    ConstantEvent constantEvent = new ConstantEvent(19);
                                    constantEvent.setControllerSeqServer(data.substring(data.indexOf(";") + 1));
                                    EventBus.getDefault().post(constantEvent);
                                    ControllerBean controllerBean = new ControllerBean();
                                    controllerBean.setSeqServer(data.substring(data.indexOf(";") + 1));
                                    controllerBean.setState("s");
                                    controllerBean.setFlag("2");
                                    Gson gson = new Gson();
                                    String s = gson.toJson(controllerBean);
                                    sendOrder(s);
                                } else if (data.contains("videoRestart")) {
                                    ConstantEvent constantEvent = new ConstantEvent(20);
                                    constantEvent.setControllerSeqServer(data.substring(data.indexOf(";") + 1));
                                    EventBus.getDefault().post(constantEvent);
                                    ControllerBean controllerBean = new ControllerBean();
                                    controllerBean.setSeqServer(data.substring(data.indexOf(";") + 1));
                                    controllerBean.setState("s");
                                    controllerBean.setFlag("2");
                                    Gson gson = new Gson();
                                    String s = gson.toJson(controllerBean);
                                    sendOrder(s);
                                }

                            }
                        } else {
                            Log.i("tcp", "连接超时");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e instanceof SocketTimeoutException) {
                            sharedPreferences = getSharedPreferences("sp",MODE_PRIVATE);
                            SharedPreferences.Editor editor= sharedPreferences.edit();
                            editor.putString("connected","");
                            editor.commit();


                            Log.i("tcp", "连接超时，正在重连");
                            releaseSocket();
                            stopSelf();

                        } else if (e instanceof NoRouteToHostException) {
                            Log.i("tcp", "该地址不存在，请检查");
                            stopSelf();

                        } else if (e instanceof ConnectException) {
                            /*重连*/
                            releaseSocket();
                            stopSelf();

                        }

                    }

                }
            });
            /*启动连接线程*/
            connectThread.start();
        }


    }

    public Context getContext() {
        return this;
    }

    /*发送数据*/
    public void sendOrder(final String msg) {
        if (socket != null && socket.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket.getOutputStream().write(msg.getBytes());
                        socket.getOutputStream().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            /*重连*/
            releaseSocket();
        }
    }

    /*定时发送数据*/
    private void sendBeatData() {
        if (timer == null) {
            timer = new Timer();
        }

        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        outputStream = socket.getOutputStream();
                        /*这里的编码方式根据你的需求去改*/
                        outputStream.write(("test").getBytes("gbk"));
                        outputStream.flush();
                    } catch (Exception e) {
                        /*发送失败说明socket断开了或者出现了其他错误*/
                        Log.i("tcp", "断开连接，正在重连");
                        /*重连*/
                        releaseSocket();
                        e.printStackTrace();
                    }
                }
            };
        }

        timer.schedule(task, 0, 2000);
    }


    /*释放资源*/
    private void releaseSocket() {

        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

        if (outputStream != null) {
            try {
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }

        if (socket != null) {
            try {
                socket.close();

            } catch (IOException e) {
            }
            socket = null;
        }

        if (connectThread != null) {
            connectThread = null;
        }

        /*重新初始化socket*/
        if (isReConnect) {
            initSocket();
        }

    }

    private void playsuccess() {
        ToPlaySuccessBean toPlaySuccessBean = new ToPlaySuccessBean();
        toPlaySuccessBean.setProgramId(programId);
        toPlaySuccessBean.setSeqServer(seqServer);
        toPlaySuccessBean.setFlag("2");
        Gson gson = new Gson();
        String s = gson.toJson(toPlaySuccessBean);
        sendOrder(s);
    }

    public void reStartApp() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        //与正常页面跳转一样可传递序列化数据,在Launch页面内获得
        intent.putExtra("REBOOT","reboot");
        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}