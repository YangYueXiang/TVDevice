package com.boe.tvdevice.activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.boe.tvdevice.R;
import com.boe.tvdevice.base.BaseActivity;
import com.boe.tvdevice.bean.constant.Constant;
import com.boe.tvdevice.eventBus.ConstantEvent;
import com.boe.tvdevice.socketKeepLine.RealIdBean;
import com.boe.tvdevice.socketKeepLine.SocketService;
import com.boe.tvdevice.utils.LoadLocalImageUtils;
import com.boe.tvdevice.utils.SharedPreferencesUtils;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

public class PictureActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.imageview)
    ImageView imageview;
    @BindView(R.id.rl_header)
    RelativeLayout rl_header;
    @BindView(R.id.fanhui)
    ImageView fanhui;
    @BindView(R.id.tv_title)
    TextView tv_title;
    private boolean isShowTitle = false;
    private String programId;
    private SocketService socketService;
    private boolean isConnectSuccess = false;
    private ServiceConnection sc;
    private SharedPreferencesUtils sharedPreferencesUtils;
    private String houzhuiname;
    private String name;
    private String durationtime;
    private String toPlaySeqServer;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_picture;
    }

    @Override
    protected int getTitleBarId() {
        return 0;
    }

    @Override
    protected void initView() {
       // bindSocketService();
        Intent intent = getIntent();
        programId = intent.getStringExtra("programId");
        String houzhuiname = intent.getStringExtra("houzhuiname");
        String name = intent.getStringExtra("name");
        sharedPreferencesUtils.putData("currentPlayProgramId",programId);
        String s = String.valueOf(programId);
        String url = Constant.FILEPATH + s + houzhuiname;
        Bitmap bitmap = LoadLocalImageUtils.getLoacalBitmap(url);
        imageview.setImageBitmap(bitmap); //设置Bitmap
        tv_title.setText(name);
        imageview.setOnClickListener(this);
        fanhui.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        sharedPreferencesUtils = new SharedPreferencesUtils(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fanhui:
                Intent intent = new Intent(this, ListShowActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.imageview:
                if (isShowTitle) {
                    isShowTitle = false;
                    rl_header.setVisibility(View.GONE);
                } else {
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
             EventBus.getDefault().postSticky(new ConstantEvent(8));
         }else if (event.getType()==ConstantEvent.Connected){
             isConnectSuccess=true;
             bindSocketService();
         }else if (event.getType()==ConstantEvent.GiveDeviceId){
             //发送json给服务器：flag：1，realId:****,method:data;
             RealIdBean realIdBean = new RealIdBean();
             realIdBean.setFlag("1");
             realIdBean.setRealId(getDeviceSN());
             realIdBean.setMethod(event.getMethod());
             Gson gson = new Gson();
             String s = gson.toJson(realIdBean);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            Intent intent = new Intent(this, ListShowActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
