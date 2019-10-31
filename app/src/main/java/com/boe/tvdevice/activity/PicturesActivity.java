package com.boe.tvdevice.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boe.tvdevice.R;
import com.boe.tvdevice.base.BaseActivity;
import com.boe.tvdevice.bean.PhotoSetNameBean;
import com.boe.tvdevice.bean.PhotosetInfoBean;
import com.boe.tvdevice.bean.SpSaveDefault;
import com.boe.tvdevice.bean.constant.Constant;
import com.boe.tvdevice.customView.MyBanner;
import com.boe.tvdevice.eventBus.ConstantEvent;
import com.boe.tvdevice.presenter.presenterImpl.PhotosetPresenterImpl;
import com.boe.tvdevice.socketKeepLine.ControllerBean;
import com.boe.tvdevice.socketKeepLine.SocketService;
import com.boe.tvdevice.utils.FileUtil1;
import com.boe.tvdevice.utils.SharedPreferencesUtils;
import com.boe.tvdevice.view.PhotosetView;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class PicturesActivity extends BaseActivity implements View.OnClickListener, PhotosetView {

    @BindView(R.id.banner)
    MyBanner banner;
    @BindView(R.id.rl_headers)
    RelativeLayout rl_headers;
    @BindView(R.id.fanhuis)
    ImageView fanhuis;
    @BindView(R.id.tv_titles)
    TextView tv_titles;
    @BindView(R.id.rl_buuton)
    RelativeLayout rl_button;
    @BindView(R.id.img_auto)
    ImageView img_auto;
    @BindView(R.id.img_shangyige)
    ImageView img_shangyige;
    @BindView(R.id.img_xiayige)
    ImageView img_xiayige;
    @BindView(R.id.img_chongbo)
    ImageView img_chongbo;
    private ArrayList<String> images;
    private ArrayList<String> imageTitle;
    private boolean isShowTitle = false;
    private boolean isAutoPlay = true;
    private boolean isStop = false; //是否暂停 ,开始的地方判断一下
    private SharedPreferencesUtils sharedPreferencesUtils;
    private List<PhotoSetNameBean> photosetnamelist;
    private String programId;
    private SocketService socketService;
    private ServiceConnection sc;
    private int durationtime;
    private PhotosetPresenterImpl photosetPresenter;
    private List<String> filesAllName;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pictures;
    }

    @Override
    protected int getTitleBarId() {
        return 0;
    }

    @Override
    protected void initView() {
        //设置轮播图片间隔时间（不设置默认为2000）
        banner.setOnClickListener(this);
        fanhuis.setOnClickListener(this);
        img_auto.setOnClickListener(this);
        img_shangyige.setOnClickListener(this);
        img_chongbo.setOnClickListener(this);
        img_xiayige.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        sharedPreferencesUtils = new SharedPreferencesUtils(this);
        Intent intent = getIntent();
        programId = intent.getStringExtra("programId");
        sharedPreferencesUtils.putData("currentPlayProgramId", programId);
        //bindSocketService();
        banner.setBannerStyle(BannerConfig.NOT_INDICATOR);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置标题集合（当banner样式有显示title时）
        //  banner.setBannerTitles(imageTitle);
        //设置轮播样式（没有标题默认为右边,有标题时默认左边）
        //可选样式:
        //Banner.LEFT   指示器居左
        //Banner.CENTER 指示器居中
        //Banner.RIGHT  指示器居右
        // banner.setIndicatorGravity(BannerConfig.CENTER);
        //设置是否允许手动滑动轮播图
        //   banner.setViewPagerIsScroll(true);
        //设置是否自动轮播（不设置则默认自动）
        banner.isAutoPlay(true);
        //设置图片资源:url或本地资源
        images = new ArrayList<>();
        Boolean aBoolean = hasNet();
        if (!aBoolean){
            //无网络时图片集的顺序和间隔时间
            String photosetsOrder = (String) sharedPreferencesUtils.getData("photosetsOrder","");
            int durationtime = (int) sharedPreferencesUtils.getData("durationtime",2000);

            ArrayList<String> sorts = new ArrayList<>();
            String[] strs = photosetsOrder.split(",");
            for (int i = 0; i < strs.length; i++) {
                sorts.add(strs[i]);
            }
            String url = Constant.FILEPATH + "photoset" + programId;
            filesAllName = FileUtil1.getFilesAllName(url);
            for (int j = 0; j < sorts.size(); j++) {
                for (int i = 0; i < filesAllName.size(); i++) {
                    if (filesAllName.get(i).contains(sorts.get(j))) {
                        images.add("file://" + Constant.FILEPATH + "photoset" + programId + "/" + filesAllName.get(i));
                    }
                }
            }
            //设置播放间隔时间
            banner.setDelayTime(durationtime);
            //设置图片资源:可选图片网址/资源文件，默认用Glide加载,也可自定义图片的加载框架
            //所有设置参数方法都放在此方法之前执行
            //  banner.setIndicatorGravity(BannerConfig.CENTER);
            banner.setImages(images)
                    .setOnBannerListener(new OnBannerListener() {
                        private String name;
                        @Override
                        public void OnBannerClick(int position) {
                            if (isShowTitle) {
                                isShowTitle = false;
                                rl_headers.setVisibility(View.GONE);
                                rl_button.setVisibility(View.GONE);
                            } else {
                                isShowTitle = true;
                                rl_headers.setVisibility(View.VISIBLE);
                                rl_button.setVisibility(View.VISIBLE);
                            }
                            List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                            for (int i = 0; i < listdata.size(); i++) {
                                if (String.valueOf(listdata.get(i).getProgramId()).equals(programId)) {
                                    name = listdata.get(i).getName();
                                }
                            }
                            // tv_titles.setText(imageTitle.get(position));
                            tv_titles.setText(name);
                            //    Toast.makeText(PicturesActivity.this, "你点了第" + (position + 1) + "张轮播图", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .start();
        }else {
            photosetPresenter = new PhotosetPresenterImpl(this);
            photosetPresenter.obtainData(programId);
        }
        EventBus.getDefault().register(this);

        /*//设置图片标题:自动对应
        imageTitle = new ArrayList<>();
        photosetnamelist = sharedPreferencesUtils.getlistdataname("photosetnamelist");
        for (int i = 0; i < photosetnamelist.size(); i++) {
            if (photosetnamelist.get(i).getProgramId().equals(programId)) {
                List<PhotoSetNameBean.DataBean> dataBeans = photosetnamelist.get(i).getDataBeans();
                for (int j = 0; j < filesAllName.size(); j++) {
                    for (int f = 0; f < dataBeans.size(); f++) {
                        if (filesAllName.get(j).contains(dataBeans.get(f).getPhotosetId())) {
                            imageTitle.add(dataBeans.get(f).getName());
                        }
                    }
                }
            }
        }*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fanhuis:
                Intent intent = new Intent(this, ListShowActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.img_auto:
                if (isAutoPlay) {
                    isAutoPlay = false;
                    banner.stopAutoPlay();
                    img_auto.setImageResource(R.drawable.auto);
                } else {
                    isAutoPlay = true;
                    banner.startAutoPlay();
                    img_auto.setImageResource(R.drawable.autocopy);
                }

                break;
            case R.id.img_shangyige:
                isAutoPlay = false;
                int presentPosition = banner.getCurrentItem();
                if (presentPosition == 0) {
                    presentPosition = images.size() - 1;
                } else {
                    presentPosition--;
                }
                banner.setCurrentItem(presentPosition);
                img_auto.setImageResource(R.drawable.auto);
                break;
            case R.id.img_chongbo:
                banner.start();
                isAutoPlay = true;
                banner.startAutoPlay();
                img_auto.setImageResource(R.drawable.autocopy);
                break;
            case R.id.img_xiayige:
                isAutoPlay = false;
                banner.stopAutoPlay();
                int currentItem = banner.getCurrentItem();
                Log.i("lsy", "onClick: " + currentItem);
                if (currentItem < images.size()) {
                    currentItem++;
                } else {
                    currentItem = 1;
                }
                img_auto.setImageResource(R.drawable.auto);
                banner.setCurrentItem(currentItem);
                break;
        }
    }

    @Override
    public void onGetPhotosetInfo(PhotosetInfoBean photosetInfoBean, String errorMsg) {
        if (photosetInfoBean.getState().equals("s")) {
            //设置播放顺序
            ArrayList<String> sorts = new ArrayList<>();
            String photosetIds = photosetInfoBean.getPhotosetIds();
            sharedPreferencesUtils.putData("photosetsOrder",photosetIds);
            String[] strs = photosetIds.split(",");
            for (int i = 0; i < strs.length; i++) {
                sorts.add(strs[i]);
            }
            String url = Constant.FILEPATH + "photoset" + programId;
            filesAllName = FileUtil1.getFilesAllName(url);
            for (int j = 0; j < sorts.size(); j++) {
                for (int i = 0; i < filesAllName.size(); i++) {
                    if (filesAllName.get(i).contains(sorts.get(j))) {
                        images.add("file://" +Constant.FILEPATH + "photoset" + programId + "/" + filesAllName.get(i));
                    }
                }
            }
            //设置播放间隔时间
            durationtime = photosetInfoBean.getPlayInterval();
            int i1 = durationtime * 1000;
            sharedPreferencesUtils.putData("durationtime",i1);
            banner.setDelayTime(i1);
            //设置图片资源:可选图片网址/资源文件，默认用Glide加载,也可自定义图片的加载框架
            //所有设置参数方法都放在此方法之前执行
            //  banner.setIndicatorGravity(BannerConfig.CENTER);
            banner.setImages(images)
                    .setOnBannerListener(new OnBannerListener() {
                        private String name;
                        @Override
                        public void OnBannerClick(int position) {
                            if (isShowTitle) {
                                isShowTitle = false;
                                rl_headers.setVisibility(View.GONE);
                                rl_button.setVisibility(View.GONE);
                            } else {
                                isShowTitle = true;
                                rl_headers.setVisibility(View.VISIBLE);
                                rl_button.setVisibility(View.VISIBLE);
                            }
                            List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                            for (int i = 0; i < listdata.size(); i++) {
                                if (String.valueOf(listdata.get(i).getProgramId()).equals(programId)) {
                                    name = listdata.get(i).getName();
                                }
                            }
                            // tv_titles.setText(imageTitle.get(position));
                            tv_titles.setText(name);
                            //    Toast.makeText(PicturesActivity.this, "你点了第" + (position + 1) + "张轮播图", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .start();
        }
    }

    /**
     * 网络加载图片
     * 使用了Glide图片加载框架
     */
    public class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Glide.with(context)
                    .load((String) path)
                    .into(imageView);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        finish();
        super.onStop();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMess(ConstantEvent event) {
        if (event.getType() == ConstantEvent.NoticeToDownload) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            EventBus.getDefault().postSticky(new ConstantEvent(8));
        } else if (event.getType() == ConstantEvent.Connected) {
            bindSocketService();
        } else if (event.getType() == ConstantEvent.PicturesAuto) {
            String controllerSeqServer = event.getControllerSeqServer();
                isAutoPlay = true;
                banner.startAutoPlay();
                img_auto.setImageResource(R.drawable.autocopy);

            ControllerBean controllerBean = new ControllerBean();
            controllerBean.setSeqServer(controllerSeqServer);
            controllerBean.setState("s");
            controllerBean.setFlag("2");
            Gson gson = new Gson();
            String s = gson.toJson(controllerBean);
            socketService.sendOrder(s);
        } else if (event.getType() == ConstantEvent.PicturesStopAuto) {
            String controllerSeqServer = event.getControllerSeqServer();
                isAutoPlay = false;
                banner.stopAutoPlay();
                img_auto.setImageResource(R.drawable.auto);

            ControllerBean controllerBean = new ControllerBean();
            controllerBean.setSeqServer(controllerSeqServer);
            controllerBean.setState("s");
            controllerBean.setFlag("2");
            Gson gson = new Gson();
            String s = gson.toJson(controllerBean);
            socketService.sendOrder(s);

        } else if (event.getType() == ConstantEvent.PicturesLast) {
            String controllerSeqServer = event.getControllerSeqServer();
            isAutoPlay=false;
            banner.stopAutoPlay();
            int presentPosition = banner.getCurrentItem();
            if (presentPosition == 0) {
                presentPosition = images.size() - 1;
            } else {
                presentPosition--;
            }
            banner.setCurrentItem(presentPosition);
            img_auto.setImageResource(R.drawable.auto);

            ControllerBean controllerBean = new ControllerBean();
            controllerBean.setSeqServer(controllerSeqServer);
            controllerBean.setState("s");
            controllerBean.setFlag("2");
            Gson gson = new Gson();
            String s = gson.toJson(controllerBean);
            socketService.sendOrder(s);

        } else if (event.getType() == ConstantEvent.PicturesNext) {
            String controllerSeqServer = event.getControllerSeqServer();
            isAutoPlay=false;
            banner.stopAutoPlay();
            int currentItem = banner.getCurrentItem();
            Log.i("lsy", "onClick: " + currentItem);
            if (currentItem < images.size()) {
                currentItem++;
            } else {
                currentItem = 1;
            }
            img_auto.setImageResource(R.drawable.auto);
            banner.setCurrentItem(currentItem);

            ControllerBean controllerBean = new ControllerBean();
            controllerBean.setSeqServer(controllerSeqServer);
            controllerBean.setState("s");
            controllerBean.setFlag("2");
            Gson gson = new Gson();
            String s = gson.toJson(controllerBean);
            socketService.sendOrder(s);

        } else if (event.getType() == ConstantEvent.PicturesRestart) {
            String controllerSeqServer = event.getControllerSeqServer();
            isAutoPlay = true;
            banner.start();
            banner.startAutoPlay();
            img_auto.setImageResource(R.drawable.autocopy);

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

    /*@Override
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
