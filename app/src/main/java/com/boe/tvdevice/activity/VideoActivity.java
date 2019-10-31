package com.boe.tvdevice.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.boe.tvdevice.R;
import com.boe.tvdevice.base.BaseActivity;
import com.boe.tvdevice.bean.constant.Constant;
import com.boe.tvdevice.eventBus.ConstantEvent;
import com.boe.tvdevice.socketKeepLine.ControllerBean;
import com.boe.tvdevice.socketKeepLine.SocketService;
import com.boe.tvdevice.utils.FileUtil1;
import com.boe.tvdevice.utils.SharedPreferencesUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

public class VideoActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.videoview)
    VideoView videoview;
    @BindView(R.id.rl_header)
    RelativeLayout rl_header;
    @BindView(R.id.fanhui)
    ImageView fanhui;
    @BindView(R.id.tv_title)
    TextView tv_title;
    private String videopath;
    private boolean isShowTitle = false;
    private String programId;
    private SocketService socketService;
    private ServiceConnection sc;
    private SharedPreferencesUtils sharedPreferencesUtils;
    private int  flag=0;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_video;
    }

    @Override
    protected int getTitleBarId() {
        return 0;
    }

    @Override
    protected void initView() {
       // bindSocketService();
        //设置有进度条可以拖动快进
        MediaController localMediaController = new MediaController(this);
        videoview.setMediaController(localMediaController);
        Intent intent = getIntent();
        programId = intent.getStringExtra("programId");
        sharedPreferencesUtils.putData("currentPlayProgramId",programId);
        String name = intent.getStringExtra("name");
        String url=  Constant.FILEPATH +"video"+ programId;
        //获取这个文件夹下面所有文件名字
        List<String> filesAllName = FileUtil1.getFilesAllName(url);
        for (int i = 0; i <filesAllName.size() ; i++) {
            if (filesAllName.get(i).contains("mp4")||filesAllName.contains("mov")){
                videopath= Constant.FILEPATH +"video"+ programId +"/"+filesAllName.get(i);
                videoview.setVideoURI(Uri.parse(videopath));
                videoview.start();
            }
        }
        tv_title.setText(name);
        fanhui.setOnClickListener(this);
        videoview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isShowTitle) {
                    isShowTitle = false;
                    rl_header.setVisibility(View.GONE);
                }else{
                    isShowTitle = true;
                    rl_header.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        //循环播放
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });


        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                flag=1;
            }
        });
    }

    @Override
    protected void initData() {
        EventBus.getDefault().register(this);
        sharedPreferencesUtils = new SharedPreferencesUtils(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fanhui:
                Intent intent=new Intent(this,ListShowActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.videoview:
                if (isShowTitle) {
                    isShowTitle = false;
                    rl_header.setVisibility(View.GONE);
                }else{
                    isShowTitle = true;
                    rl_header.setVisibility(View.VISIBLE);
                }
                break;

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMess(ConstantEvent event){
        if (event.getType()==ConstantEvent.NoticeToDownload){
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
            EventBus.getDefault().postSticky(new ConstantEvent(8));
        }else if (event.getType()==ConstantEvent.Connected){
            bindSocketService();
        }else if (event.getType()==ConstantEvent.VideoPlay){
            String controllerSeqServer = event.getControllerSeqServer();
            if (!videoview.isPlaying()){
                videoview.start();
            }
            ControllerBean controllerBean = new ControllerBean();
            controllerBean.setSeqServer(controllerSeqServer);
            controllerBean.setState("s");
            controllerBean.setFlag("2");
            Gson gson = new Gson();
            String s = gson.toJson(controllerBean);
            socketService.sendOrder(s);
        }else if (event.getType()==ConstantEvent.VideoStop){
            flag=2;
            String controllerSeqServer = event.getControllerSeqServer();
            if (videoview.isPlaying()){
                videoview.pause();
            }
            ControllerBean controllerBean = new ControllerBean();
            controllerBean.setSeqServer(controllerSeqServer);
            controllerBean.setState("s");
            controllerBean.setFlag("2");
            Gson gson = new Gson();
            String s = gson.toJson(controllerBean);
            socketService.sendOrder(s);
        }else if (event.getType()==ConstantEvent.VideoRestart){
            if (flag==0){
                videoview.resume();
            }else if (flag==1){
                videoview.start();
                flag=0;
            }else {
                videoview.start();
                videoview.resume();
            }
            String controllerSeqServer = event.getControllerSeqServer();
            ControllerBean controllerBean = new ControllerBean();
            controllerBean.setSeqServer(controllerSeqServer);
            controllerBean.setState("s");
            controllerBean.setFlag("2");
            Gson gson = new Gson();
            String s = gson.toJson(controllerBean);
            socketService.sendOrder(s);
        }
    }

    private void bindSocketService() {

        /*通过binder拿到service*/
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                SocketService.SocketBinder binder = (SocketService.SocketBinder) iBinder;
                socketService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        bindService(intent, sc, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

   /* @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, ListShowActivity.class);
        startActivity(intent);
        finish();
    }*/
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
       if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
           Intent intent = new Intent(this, ListShowActivity.class);
           startActivity(intent);
           finish();
       }
       return super.onKeyDown(keyCode, event);
   }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

}
