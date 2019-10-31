package com.boe.tvdevice.base;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.boe.tvdevice.utils.ActivityCollector;
import com.gyf.barlibrary.ImmersionBar;
import com.lzy.okgo.OkGo;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.OkHttpClient;

public abstract class BaseActivity extends AppCompatActivity {
    private Unbinder mButterKnife;
    private ImmersionBar immersionBar;
    private NetworkInfo networkInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        initActivity();
        immersionBar = ImmersionBar.with(this);
        immersionBar.init();

    }


    protected void initActivity() {
        //网络
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        initLayout();
        initData();
        initView();

    }

    protected void initLayout() {
        if (getLayoutId() > 0) {
            setContentView(getLayoutId());
            mButterKnife = ButterKnife.bind(this);
        }

    }

    // 引入布局
    protected abstract int getLayoutId();

    // 标题栏id
    protected abstract int getTitleBarId();

    // 初始化控件
    protected abstract void initView();

    // 初始化数据
    protected abstract void initData();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.addActivity(this);
        if (immersionBar != null)
            immersionBar.destroy();
        if (mButterKnife != null) mButterKnife.unbind();
        //取消指定tag的请求
        OkGo.getInstance().cancelTag(this);
        //取消全部请求
        OkGo.getInstance().cancelAll();
        //取消OkHttpClient的全部请求
        OkGo.cancelAll(new OkHttpClient());
        OkGo.cancelTag(new OkHttpClient(), "且指定tag");
    }

    public Boolean hasNet() {
        //判断有没有网络
        if (networkInfo == null || !networkInfo.isAvailable()) {
            return false;
        } else {
            return true;
        }
    }

    public static String getDeviceSN() {
        String serialNumber = android.os.Build.SERIAL;
        return serialNumber;
    }

    public Context getContext(){
        return this;
    }

}
