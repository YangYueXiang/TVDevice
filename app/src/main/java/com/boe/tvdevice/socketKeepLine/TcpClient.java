package com.boe.tvdevice.socketKeepLine;

import android.util.Log;

import com.boe.tvdevice.eventBus.ConstantEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClient {

    public static Socket socket;

    public static void startClient(final String address ,final int port){
        if (address == null){
            return;
        }
        if (socket == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("tcp", "启动客户端");
                        socket = new Socket(address, port);
                        Log.i("tcp", "客户端连接成功");
                        EventBus.getDefault().postSticky(new ConstantEvent(9));
                        PrintWriter pw = new PrintWriter(socket.getOutputStream());

                        InputStream inputStream = socket.getInputStream();

                        byte[] buffer = new byte[1024];
                        int len = -1;
                        while ((len = inputStream.read(buffer)) != -1) {
                            String data = new String(buffer, 0, len);
                            Log.i("tcp", "收到服务器的数据---------------------------------------------:" + data);
                            if (data.equals("getExhibitsRealId")){
                                ConstantEvent constantEvent = new ConstantEvent(4);
                                constantEvent.setMethod(data);
                                EventBus.getDefault().post(constantEvent);
                            }else if (data.equals("downloadProgram")){
                                //通知去下载
                                EventBus.getDefault().post(new ConstantEvent(5));
                            }else if (data.contains("getCurrentProgram")){
                                //通知返回当前播放
                                ConstantEvent constantEvent = new ConstantEvent(6);
                                constantEvent.setCurrentProgramMethod("getCurrentProgram");
                                constantEvent.setSeqServer(data.substring(data.indexOf(";") + 1));
                                EventBus.getDefault().post(constantEvent);
                            }

                        }
                        Log.i("tcp", "客户端断开连接");
                        pw.close();

                    } catch (Exception EE) {
                        EE.printStackTrace();
                        Log.i("tcp", "客户端无法连接服务器");

                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        socket = null;
                    }
                }
            }).start();
        }
    }

    public static void sendTcpMessage(final String msg){
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
        }
    }
}
