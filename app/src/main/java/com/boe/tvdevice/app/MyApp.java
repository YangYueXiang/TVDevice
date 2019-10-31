package com.boe.tvdevice.app;

import android.app.Application;
import android.os.Environment;

import com.boe.tvdevice.utils.ToastMgr;
import com.lzy.okgo.OkGo;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.task.XExecutor;

import java.io.File;

public class MyApp extends Application {
    private static MyApp application;
    @Override
    public void onCreate() {
        super.onCreate();
        ToastMgr.init(this);
        OkGo.getInstance().init(this);
        OkDownload okDownload = OkDownload.getInstance();
        okDownload.setFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ZHZT" + File.separator); //设置全局下载目录
        okDownload.getThreadPool().setCorePoolSize(1); //设置同时下载数量
        XExecutor.OnAllTaskEndListener listener = new XExecutor.OnAllTaskEndListener() {
            @Override
            public void onAllTaskEnd() {
               /* Intent intent=new Intent(getApplicationContext(), ListShowActivity.class);
                startActivity(intent);*/
            }
        };
        okDownload.addOnAllTaskEndListener(listener); //设置所有任务监听
      //  initOkGo();
    }

    private void initOkGo() {
        /*//okGo网络框架初始化和全局配置
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));      //使用sp保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));//使用数据库保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));      //使用内存保持cookie，app退出后，cookie消失
        //设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文，不允许有特殊字符
        headers.put("commonHeaderKey2", "commonHeaderValue2");
        //设置请求参数
        HttpParams params = new HttpParams();
        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
        params.put("commonParamsKey2", "这里支持中文参数");
        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置会使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3)                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
                .addCommonHeaders(headers)                      //全局公共头
                .addCommonParams(params);*/

    }
    public static MyApp getInstance() {
        return application;
    }
}
