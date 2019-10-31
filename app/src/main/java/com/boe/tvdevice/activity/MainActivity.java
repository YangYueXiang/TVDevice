package com.boe.tvdevice.activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.boe.tvdevice.R;
import com.boe.tvdevice.base.BaseActivity;
import com.boe.tvdevice.bean.Bean;
import com.boe.tvdevice.bean.PhotoSetNameBean;
import com.boe.tvdevice.bean.SpSaveDefault;
import com.boe.tvdevice.bean.constant.Constant;
import com.boe.tvdevice.customView.DonloadView;
import com.boe.tvdevice.eventBus.ConstantEvent;
import com.boe.tvdevice.presenter.presenterImpl.PresenterImpl;
import com.boe.tvdevice.socketKeepLine.DownLoadFailBean;
import com.boe.tvdevice.socketKeepLine.SocketService;
import com.boe.tvdevice.utils.FileSizeUtils;
import com.boe.tvdevice.utils.FileUtil1;
import com.boe.tvdevice.utils.SharedPreferencesUtils;
import com.boe.tvdevice.utils.ShengYuCunChuSizeUtil;
import com.boe.tvdevice.utils.ToastMgr;
import com.boe.tvdevice.view.View;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadListener;
import com.lzy.okserver.download.DownloadTask;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import butterknife.BindView;
import butterknife.OnClick;
import static com.lzy.okgo.model.Progress.FINISH;
import static com.lzy.okgo.model.Progress.LOADING;

public class MainActivity extends BaseActivity implements View {

    @BindView(R.id.ll_loading)
    LinearLayout llLoading;
    @BindView(R.id.btn_into)
    Button btnInto;
    @BindView(R.id.ll_download)
    LinearLayout llDownload;
    private PresenterImpl presenter;
    private SharedPreferencesUtils sharedPreferencesUtils;
    private String localPathPhotoSet;
    private String localPathVideo;
    private List<String> unlineProgramIdList;
    private String cutName;
    private Bean bean;
    private static DonloadView start;
    private long sum;
    private Timer timer;
    private TimerTask task;
    private TextView tv_shengyutime;
    private NetworkInfo networkInfo;
    private ServiceConnection sc;
    private static SocketService socketService;
    private static int flag = 0;
    private double v;
    private double v1;
    private long filesize;
    private TextView tv_progress;
    private static long lastfilesize = 0;
    private int ha;
    private SharedPreferences sharedPreferences;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getTitleBarId() {
        return 0;
    }

    @Override
    protected void initView() {
        socketKeepLine();
        sharedPreferencesUtils = new SharedPreferencesUtils(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("yyx", "onresume");
        SharedPreferences sharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE);
        String connected = sharedPreferences.getString("connected", "");
        if (connected.equals("connected")) {
            if (presenter == null) {
                presenter = new PresenterImpl(this);
                presenter.obtainData(getDeviceSN());
            } else {
                presenter.obtainData(getDeviceSN());
            }
        }
    }

    private void socketKeepLine() {
        /*先判断 Service是否正在运行 如果正在运行  给出提示  防止启动多个service*/
        if (isServiceRunning("com.boe.tvdevice.socketKeepLine.SocketService")) {
            Toast.makeText(this, "连接服务已运行", Toast.LENGTH_SHORT).show();
            bindSocketService();
        } else {
            /*启动service*/
            Intent intent = new Intent(getApplicationContext(), SocketService.class);
            startService(intent);
        }
    }

    /**
     * 判断服务是否运行
     */
    private boolean isServiceRunning(final String className) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }

    @Override
    protected void initData() {
        File file = new File(Constant.FILEPATH);
        if (file.exists()) {
            lastfilesize = (int) FileSizeUtils.getFolderSize(file);
        } else {
            lastfilesize = 0;
        }
        EventBus.getDefault().register(this);
        tv_shengyutime = findViewById(R.id.tv_shengyutime);
        start = findViewById(R.id.start);
        tv_progress = findViewById(R.id.tv_progress);
        //网络
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        //判断有没有网络
        if (networkInfo == null || !networkInfo.isAvailable()) {
            //没网
            Intent intent=new Intent(this,ListShowActivity.class);
            startActivity(intent);
            finish();
        } else {
            //写个定时器,计算百分比和已下载文件的大小进度
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    long l1 = filesize - lastfilesize;
                    //获取本地文件夹的大小
                    String path =Constant.FILEPATH;
                    File file = new File(path);
                    filesize = (int) FileSizeUtils.getFolderSize(file);
                    v = FileSizeUtils.FormetFileSize(filesize - lastfilesize, 3);
                    long l = filesize - lastfilesize;
                    Log.i("lastfilesize", l + "");
                    DecimalFormat df = new DecimalFormat("#.00");
                    v1 = Double.valueOf(df.format(sum / 1024));
                    int floor;
                    tv_shengyutime.post(new Runnable() {
                        @Override
                        public void run() {
                            if (l1 > 0) {
                                if (l1 > 1024 && sum > 1024) {
                                    if (MainActivity.this.v < v1) {
                                        tv_shengyutime.setText(MainActivity.this.v + "MB/" + v1 + "MB");
                                    }
                                } else {
                                    if (filesize - lastfilesize < sum) {
                                        tv_shengyutime.setText(l1 + "KB/" + sum + "KB");
                                    }
                                }
                            }
                        }
                    });
                    if (v < v1 && v > 0) {
                        double v = MainActivity.this.v / v1;
                        double v2 = v * 100;
                        floor = (int) Math.floor(v2);
                        tv_progress.post(new Runnable() {
                            @Override
                            public void run() {
                                tv_progress.setText(floor + "%");
                            }
                        });
                    }
                }
            };
            timer.schedule(task, 1000, 2000);
        }
    }

    @OnClick(R.id.btn_into)
    public void onViewClicked() {
        flag = 1;
        task.cancel();
        timer.cancel();
        DownLoadFileUtils.removeAllDownload();
        OkDownload.getInstance().removeAll();
        updatedefaultplay();
        //图片集的封面同步
        List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
        ArrayList<String> stringstype = new ArrayList<>();
        for (int i = 0; i <listdata.size(); i++) {
            stringstype.add(listdata.get(i).getType());
        }

        if (stringstype.contains("3")){
            for (int i = 0; i <listdata.size() ; i++) {
                if (listdata.get(i).getType().equals("3")){
                    for (int j = 0; j <bean.getData().size() ; j++) {
                        if (String.valueOf(bean.getData().get(j).getProgramId()).equals(listdata.get(i).getProgramId()+"")){
                            listdata.get(i).setCoverId(bean.getData().get(j).getCoverId());
                        }
                    }
                }
            }
            sharedPreferencesUtils.setlistdata("listdata",listdata);
        }
    }

    private void updatedefaultplay() {
        //默认播放的同步
        String programIddefault = null;
        List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
        for (int i = 0; i <bean.getData().size() ; i++) {
            if (bean.getData().get(i).getDefaultX().equals("1")){
                programIddefault=bean.getData().get(i).getProgramId()+"";
            }
        }

        ArrayList<String> strings1 = new ArrayList<>();
        for (int i = 0; i <listdata.size() ; i++) {
            strings1.add(listdata.get(i).getProgramId()+"");
        }
        if (strings1.contains(programIddefault)){
            for (int i = 0; i <listdata.size() ; i++) {
                String programId = listdata.get(i).getProgramId()+"";
                String name = listdata.get(i).getName();
                if (programIddefault.equals(listdata.get(i).getProgramId()+"")){
                    if (listdata.get(i).getType().equals("1")){
                        String houzhuiname = listdata.get(i).getHouzhuiname();
                        Intent intent = new Intent(this, PictureActivity.class);
                        intent.putExtra("programId", programId);
                        intent.putExtra("houzhuiname", houzhuiname);
                        intent.putExtra("name", name);
                        startActivity(intent);
                        finish();
                    }else if (listdata.get(i).getType().equals("2")){
                        Intent intent = new Intent(this, VideoActivity.class);
                        intent.putExtra("programId", programId);
                        intent.putExtra("name", name);
                        startActivity(intent);
                        finish();
                    }else if (listdata.get(i).getType().equals("3")){
                        Intent intent = new Intent(this, PicturesActivity.class);
                        intent.putExtra("programId", programId);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }else {
            Intent intent = new Intent(this, ListShowActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onGetData(Bean bean, String errorMsg) {
        if (bean != null && bean.getState().equals("s")) {
            //获取要下载文件的大小
            sum = bean.getSum();
            //获取本地剩余存储
            long l = ShengYuCunChuSizeUtil.readSystem();
            if (l < sum) {
                //返回内存不足
                ConstantEvent constantEvent = new ConstantEvent(21);
                constantEvent.setNeicunState("0");
                EventBus.getDefault().post(constantEvent);
            }

            if (bean.getData().size() > 0) {
                this.bean = bean;
                //进行下载
                download();
            } else {
                //删除所有
                FileUtil1.deleteDirectory(Constant.FILEPATH);
                sharedPreferencesUtils.clean(this);

                SharedPreferences sharedPreferences = getSharedPreferences("onlyonetag", Context.MODE_PRIVATE);
                int onetag = sharedPreferences.getInt("onetag", 0);
                Log.i("cleanonetag", onetag + "");
                DownLoadFailBean downLoadFailBean = new DownLoadFailBean();
                downLoadFailBean.setFlag("1");
                downLoadFailBean.setMemory("1");
                downLoadFailBean.setMethod("downloadProgram");
                downLoadFailBean.setRealId(getDeviceSN());
                downLoadFailBean.setFailProgramId("");
                Gson gson = new Gson();
                String s1 = gson.toJson(downLoadFailBean);
                socketService.sendOrder(s1);
                task.cancel();
                Intent intent = new Intent(this, ListShowActivity.class);
                startActivity(intent);
                finish();

            }
        } else {
            sharedPreferences = getSharedPreferences("sp", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("connected", "");
            editor.commit();
            ToastMgr.show("无网络");
        }

    }

    private void download() {
        int allsize = 0;
        int www = 0;
        String localPath = DownLoadFileUtils.customLocalStoragePath("ZHZT");// /storage/emulated/0/image/\   //下载全部
        //获取线下文件夹的programId集合
      //  unlineProgramIdList = sharedPreferencesUtils.getDataList("unlineProgramIdList");
        unlineProgramIdList= new ArrayList<String>();
        List<SpSaveDefault> listdata1 = sharedPreferencesUtils.getlistdata("listdata");
        for (int i = 0; i <listdata1.size() ; i++) {
            unlineProgramIdList.add(listdata1.get(i).getProgramId()+"");
        }
        //获取线上的programId存入集合
        ArrayList<String> lineProgramIdList = new ArrayList<>();
        for (int i = 0; i < bean.getData().size(); i++) {
            lineProgramIdList.add(bean.getData().get(i).getProgramId() + "");
            Log.i("yyxline", lineProgramIdList.get(i));
        }

        File file = new File(localPath);
        if (file.exists()) {
            //去除两个集合中相同的元素
            //1.创建两个集合的副本
            List<String> linefuben = new ArrayList<>(lineProgramIdList);
            List<String> unlinefuben = new ArrayList<>(unlineProgramIdList);
            //2.去除两个集合相同的数据
            linefuben.removeAll(unlineProgramIdList);
            unlinefuben.removeAll(lineProgramIdList);
            //线上集合剩下的需要去下载
            if (unlinefuben.size() > 0) {
                //线下集合剩下的programId，是线上没有的，从文件中删除掉
                //首先获取文件夹下所有文件名称
                List<String> filesAllName = FileUtil1.getFilesAllName(Constant.FILEPATH);

                for (int i = 0; i < filesAllName.size(); i++) {
                    for (int j = 0; j < unlinefuben.size(); j++) {
                        if (filesAllName.get(i).contains(unlinefuben.get(j))) {
                            File file1 = new File(Constant.FILEPATH + filesAllName.get(i));
                            if (file1.isFile()) {
                                FileUtil1.deleteFile(Constant.FILEPATH + filesAllName.get(i));
                            } else {
                                FileUtil1.deleteDirectory(Constant.FILEPATH + filesAllName.get(i));
                            }
                            //把listdata  sp里面的数据同步减少
                            List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                            for (int k = 0; k < listdata.size(); k++) {
                                if (String.valueOf(listdata.get(k).getProgramId()).equals(unlinefuben.get(j))) {
                                    listdata.remove(listdata.get(k));
                                }
                            }
                            Log.e("afterlistsize", listdata.size() + "");
                            sharedPreferencesUtils.setlistdata("listdata", listdata);
                            //unlineProgramId  sp数据同样同步减少
                            List<String> unlineProgramIdList1 = sharedPreferencesUtils.getDataList("unlineProgramIdList");
                            for (int k = 0; k < unlineProgramIdList1.size(); k++) {
                                if (unlinefuben.get(j).equals(unlineProgramIdList1.get(k))) {
                                    unlineProgramIdList1.remove(unlineProgramIdList1.get(k));
                                }
                            }
                            sharedPreferencesUtils.setDataList("unlineProgramIdList", unlineProgramIdList1);
                            //photonameslist sp 同样去掉
                            List<PhotoSetNameBean> photosetnamelist = sharedPreferencesUtils.getlistdataname("photosetnamelist");
                            for (int k = 0; k < photosetnamelist.size(); k++) {
                                if (photosetnamelist.get(k).getProgramId().equals(unlinefuben.get(j))) {
                                    photosetnamelist.remove(photosetnamelist.get(k));
                                }
                            }
                            sharedPreferencesUtils.setlistdataname("photosetnamelist", photosetnamelist);
                            //photosetprogramIdlist删除
                            List<String> photosetprogramIdlist1 = sharedPreferencesUtils.getDataList("photosetprogramIdlist");
                            for (int k = 0; k < photosetprogramIdlist1.size(); k++) {
                                if (photosetprogramIdlist1.get(k).equals(unlinefuben.get(j))) {
                                    photosetprogramIdlist1.remove(photosetprogramIdlist1.get(k));
                                }
                            }
                            sharedPreferencesUtils.setDataList("photosetprogramIdlist", photosetprogramIdlist1);
                        }
                    }
                }
            }
            if (linefuben.size() > 0) {
                //所有文件的个数
                for (int i = 0; i < bean.getData().size(); i++) {
                    for (int j = 0; j < linefuben.size(); j++) {
                        if (linefuben.get(j).equals(String.valueOf(bean.getData().get(i).getProgramId()))) {
                            if (bean.getData().get(i).getType().equals("1")) {
                                allsize++;
                            } else if (bean.getData().get(i).getType().equals("2")) {
                                allsize += 2;
                            } else if (bean.getData().get(i).getType().equals("3")) {
                                allsize += bean.getData().get(i).getPhotosets().size();
                            }
                        }
                    }
                }
                //allsize 里面加入要下得封面的个数
                List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                for (int i = 0; i <bean.getData().size() ; i++) {
                    for (int j = 0; j <listdata.size() ; j++) {
                        if (listdata.get(j).getType().equals("2")&&String.valueOf(listdata.get(j).getProgramId()).equals(bean.getData().get(i).getProgramId()+"")){
                            if (!listdata.get(j).getVideocoverurl().equals(bean.getData().get(i).getCoverName())){
                                allsize++;
                            }
                        }
                    }
                }

                llLoading.setVisibility(android.view.View.GONE);
                llDownload.setVisibility(android.view.View.VISIBLE);
                start.start();

                for (int i = 0; i < bean.getData().size(); i++) {
                    if (linefuben.contains(String.valueOf(bean.getData().get(i).getProgramId()))) {
                        if (bean.getData().get(i).getType().equals("1")) {
                            //去下载对应的id节目
                            String filepath = bean.getData().get(i).getFilepath().replace("10.251.159.9", "218.104.69.90");
                            String gethouzhuiming = FileUtil1.gethouzhuiming(filepath);
                            cutName = DownLoadFileUtils.subFileFullName(bean.getData().get(i).getProgramId() + "", filepath);
                            DownLoadFileUtils.downloadFile(this, filepath, localPath, cutName, filepath, bean.getData().get(i).getProgramId() + "", "1", bean.getData().get(i).getName(), gethouzhuiming, 0, sharedPreferencesUtils, allsize + www, bean.getData().get(i).getDefaultX(), "", "", "", bean, 0, "", "","");
                        } else if (bean.getData().get(i).getType().equals("3")) {
                            for (int j = 0; j < bean.getData().get(i).getPhotosets().size(); j++) {
                                localPathPhotoSet = DownLoadFileUtils.customLocalStoragePath("ZHZT/" + "photoset" + bean.getData().get(i).getProgramId());
                                String cutName = DownLoadFileUtils.subFileFullName(bean.getData().get(i).getPhotosets().get(j).getPhotosetId() + "", bean.getData().get(i).getPhotosets().get(j).getFilepath().replace("10.251.159.9", "218.104.69.90") + "");
                                DownLoadFileUtils.downloadFile(this, bean.getData().get(i).getPhotosets().get(j).getFilepath().replace("10.251.159.9", "218.104.69.90") + "", localPathPhotoSet, cutName, bean.getData().get(i).getPhotosets().get(j).getFilepath().replace("10.251.159.9", "218.104.69.90") + "", bean.getData().get(i).getProgramId() + "", "3", bean.getData().get(i).getPhotosets().get(j).getName(), "", bean.getData().get(i).getPhotosets().size(), sharedPreferencesUtils, allsize + www, bean.getData().get(i).getDefaultX(), bean.getData().get(i).getName(), bean.getData().get(i).getPlanInterval() + "", bean.getData().get(i).getPhotosets().get(j).getPhotosetId() + "", bean, 0, bean.getData().get(i).getCoverId(), bean.getData().get(i).getPhotosets().size() + "","");
                            }
                        } else if (bean.getData().get(i).getType().equals("2")) {
                            localPathVideo = DownLoadFileUtils.customLocalStoragePath("ZHZT/" + "video" + bean.getData().get(i).getProgramId());
                            String cutName = DownLoadFileUtils.subFileFullName("video", bean.getData().get(i).getFilepath().replace("10.251.159.9", "218.104.69.90"));
                            String cutNamecoverimg = DownLoadFileUtils.subFileFullName("coverimg", bean.getData().get(i).getCoverName().replace("10.251.159.9", "218.104.69.90"));
                            DownLoadFileUtils.downloadFile(this, bean.getData().get(i).getFilepath().replace("10.251.159.9", "218.104.69.90") + "", localPathVideo, cutName, bean.getData().get(i).getFilepath().replace("10.251.159.9", "218.104.69.90") + "", bean.getData().get(i).getProgramId() + "", "2", bean.getData().get(i).getName(), "", 0, sharedPreferencesUtils, allsize + www, bean.getData().get(i).getDefaultX(), "", "", "", bean, 0, "", "","");
                            DownLoadFileUtils.downloadFile(this, bean.getData().get(i).getCoverName().replace("10.251.159.9", "218.104.69.90") + "", localPathVideo, cutNamecoverimg, bean.getData().get(i).getCoverName().replace("10.251.159.9", "218.104.69.90") + "", bean.getData().get(i).getProgramId() + "", "2", bean.getData().get(i).getName(), "", 0, sharedPreferencesUtils, allsize + www, bean.getData().get(i).getDefaultX(), "", "", "", bean, 0, "", "",bean.getData().get(i).getCoverName());
                        }
                    }

                }

            } else {
                DownLoadFailBean downLoadFailBean = new DownLoadFailBean();
                downLoadFailBean.setFlag("1");
                downLoadFailBean.setMemory("1");
                downLoadFailBean.setMethod("downloadProgram");
                downLoadFailBean.setRealId(getDeviceSN());
                downLoadFailBean.setFailProgramId("");
                Gson gson = new Gson();
                String s1 = gson.toJson(downLoadFailBean);
                socketService.sendOrder(s1);

                List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                //图片集的封面同步
                ArrayList<String> strings = new ArrayList<>();
                for (int i = 0; i <listdata.size(); i++) {
                    strings.add(listdata.get(i).getType());
                }

                if (strings.contains("3")){
                    for (int i = 0; i <listdata.size() ; i++) {
                        if (listdata.get(i).getType().equals("3")){
                            for (int j = 0; j <bean.getData().size() ; j++) {
                                if (String.valueOf(bean.getData().get(j).getProgramId()).equals(listdata.get(i).getProgramId()+"")){
                                    listdata.get(i).setCoverId(bean.getData().get(j).getCoverId());
                                }
                            }
                        }
                    }
                    sharedPreferencesUtils.setlistdata("listdata",listdata);
                }


                //如果不下载封面，则跳转
                ArrayList<String> unlinevideocoverlist = new ArrayList<>();
                ArrayList<String> linevideocoverlist = new ArrayList<>();
                for (int i = 0; i <listdata.size() ; i++) {
                    unlinevideocoverlist.add(listdata.get(i).getVideocoverurl());
                }
                for (int i = 0; i <bean.getData().size() ; i++) {
                    linevideocoverlist.add(bean.getData().get(i).getCoverName());
                }
                //1.创建两个集合的副本
                List<String> linevideourlfuben = new ArrayList<>(linevideocoverlist);
                List<String> unlinevideourlfuben = new ArrayList<>(unlinevideocoverlist);
                //2.去除两个集合相同的数据
                linevideourlfuben.removeAll(unlinevideocoverlist);
                unlinevideourlfuben.removeAll(linevideocoverlist);
                if (linevideourlfuben.size()==0&&unlinevideourlfuben.size()==0){
                    updatedefaultplay();
                }

            }



            //判断这个图片集是否下载过，如果下载过，遍历本地的图片，看是否与线上的一致，若否，多了就删除，少了就下载

            if (unlineProgramIdList.size() > 0) {
                for (int i = 0; i < bean.getData().size(); i++) {
                    if (bean.getData().get(i).getType().equals("3")) {
                        for (int j = 0; j < unlineProgramIdList.size(); j++) {
                            if (unlineProgramIdList.get(j).equals(bean.getData().get(i).getProgramId() + "")) {
                                //获取本地图片集中的图片id与线上的对比
                                ArrayList<String> unlinenames = new ArrayList<>();
                                ArrayList<String> unlineids = new ArrayList<>();
                                ArrayList<String> lineids = new ArrayList<>();
                                //首先获取文件夹下所有文件名称
                                List<String> filesAllName = FileUtil1.getFilesAllName(Constant.FILEPATH + "photoset" + unlineProgramIdList.get(j) + "/");
                                for (int k = 0; k < filesAllName.size(); k++) {
                                    unlinenames.add(filesAllName.get(k));
                                    String substring = filesAllName.get(k).substring(0, filesAllName.get(k).lastIndexOf("."));
                                    unlineids.add(substring);
                                }
                                //与线上的id进行对比，修改名称
                                List<PhotoSetNameBean> photosetnamelist = sharedPreferencesUtils.getlistdataname("photosetnamelist");
                                for (int l = 0; l < photosetnamelist.size(); l++) {
                                    if (String.valueOf(bean.getData().get(i).getProgramId()).equals(photosetnamelist.get(l).getProgramId())) {
                                        for (int k = 0; k < photosetnamelist.get(l).getDataBeans().size(); k++) {
                                            for (int m = 0; m < bean.getData().get(i).getPhotosets().size(); m++) {
                                                if (photosetnamelist.get(l).getDataBeans().get(k).getPhotosetId().equals(bean.getData().get(i).getPhotosets().get(m).getPhotosetId())) {
                                                    photosetnamelist.get(l).getDataBeans().get(k).setName(bean.getData().get(i).getPhotosets().get(m).getName());
                                                }
                                            }
                                        }
                                    }
                                }
                                sharedPreferencesUtils.setlistdataname("photosetnamelist", photosetnamelist);

                                //获取当前图片集所有图片id
                                for (int k = 0; k < bean.getData().get(i).getPhotosets().size(); k++) {
                                    lineids.add(bean.getData().get(i).getPhotosets().get(k).getPhotosetId() + "");
                                }
                                //1.创建两个集合的副本
                                List<String> lineidsfuben = new ArrayList<>(lineids);
                                List<String> unlineidsfuben = new ArrayList<>(unlineids);
                                //2.去除两个集合相同的数据
                                lineidsfuben.removeAll(unlineids);
                                unlineidsfuben.removeAll(lineids);
                                //多了就删除，少了就下载
                                if (unlineidsfuben.size() > 0) {
                                    //删除
                                    for (int k = 0; k < unlineidsfuben.size(); k++) {
                                        for (int l = 0; l < unlinenames.size(); l++) {
                                            if (unlinenames.get(l).contains(unlineidsfuben.get(k))) {
                                                FileUtil1.deleteFile(Constant.FILEPATH + "photoset" + unlineProgramIdList.get(j) + "/" + unlinenames.get(l) + "/");
                                            }
                                        }
                                    }
                                    //photonameslist sp 同样去掉
                                    List<PhotoSetNameBean> photosetnamelist1 = sharedPreferencesUtils.getlistdataname("photosetnamelist");
                                    for (int q = 0; q < photosetnamelist1.size(); q++) {
                                        if (photosetnamelist1.get(q).getProgramId().equals(unlineProgramIdList.get(j))) {
                                            for (int k = 0; k < photosetnamelist1.get(q).getDataBeans().size(); k++) {
                                                if (unlineidsfuben.contains(photosetnamelist1.get(q).getDataBeans().get(k).getPhotosetId())) {
                                                    photosetnamelist1.get(q).getDataBeans().remove(k);
                                                }
                                            }

                                        }
                                    }
                                    sharedPreferencesUtils.setlistdataname("photosetnamelist", photosetnamelist1);
                                }
                                if (lineidsfuben.size() > 0) {
                                    //下载
                                    llLoading.setVisibility(android.view.View.GONE);
                                    llDownload.setVisibility(android.view.View.VISIBLE);
                                    start.start();
                                    for (int k = 0; k < lineidsfuben.size(); k++) {
                                        for (int l = 0; l < bean.getData().get(i).getPhotosets().size(); l++) {
                                            if ((bean.getData().get(i).getPhotosets().get(l).getPhotosetId() + "").equals(lineidsfuben.get(k))) {
                                                allsize++;
                                                localPathPhotoSet = DownLoadFileUtils.customLocalStoragePath("ZHZT/" + "photoset" + unlineProgramIdList.get(j));
                                                String cutName = DownLoadFileUtils.subFileFullName(lineidsfuben.get(k) + "", bean.getData().get(i).getPhotosets().get(l).getFilepath().replace("10.251.159.9", "218.104.69.90") + "");
                                                DownLoadFileUtils.downloadFile(this, bean.getData().get(i).getPhotosets().get(l).getFilepath().replace("10.251.159.9", "218.104.69.90") + "", localPathPhotoSet, cutName, bean.getData().get(i).getPhotosets().get(l).getFilepath().replace("10.251.159.9", "218.104.69.90") + "", bean.getData().get(i).getProgramId() + "", "3", bean.getData().get(i).getPhotosets().get(l).getName(), "", bean.getData().get(i).getPhotosets().size(), sharedPreferencesUtils, allsize + www, bean.getData().get(i).getDefaultX(), bean.getData().get(i).getName(), bean.getData().get(i).getPlanInterval() + "", bean.getData().get(i).getPhotosets().get(l).getPhotosetId() + "", bean, 0, bean.getData().get(i).getCoverId(), bean.getData().get(i).getPhotosets().size() + "","");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (bean.getData().get(i).getType().equals("2")) {
                        int newfengmian = (int) sharedPreferencesUtils.getData("newfengmian", 0);
                        newfengmian++;
                        sharedPreferencesUtils.putData("newfengmian",newfengmian);
                        //封面下载
                            List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                            for (int k = 0; k <listdata.size() ; k++) {
                                if (listdata.get(k).getType().equals("2")&&String.valueOf(listdata.get(k).getProgramId()).equals(bean.getData().get(i).getProgramId()+"")){
                                        if (!listdata.get(k).getVideocoverurl().equals(bean.getData().get(i).getCoverName())){
                                            localPathVideo = DownLoadFileUtils.customLocalStoragePath("ZHZT/" + "video" + bean.getData().get(i).getProgramId());
                                            String cutNamecoverimg = DownLoadFileUtils.subFileFullName("coverimg"+newfengmian, bean.getData().get(i).getCoverName());
                                            DownLoadFileUtils.downloadFile(this, bean.getData().get(i).getCoverName() + "", localPathVideo, cutNamecoverimg, bean.getData().get(i).getCoverName()+ "", bean.getData().get(i).getProgramId() + "", "2", bean.getData().get(i).getName(), "", 0, sharedPreferencesUtils, allsize, bean.getData().get(i).getDefaultX(), "", "", "", bean, www, "", "",bean.getData().get(i).getCoverName());
                                        }
                                }
                            }
                    }
                }
                //判断图片,图片集，视频有没有下载过，如果下载过，看是否标题与线上的一致
                List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                for (int j = 0; j < bean.getData().size(); j++) {
                    for (int k = 0; k < listdata.size(); k++) {
                        if (listdata.get(k).getProgramId() == bean.getData().get(j).getProgramId()) {
                            listdata.get(k).setName(bean.getData().get(j).getName());
                        }
                    }
                }
                sharedPreferencesUtils.setlistdata("listdata", listdata);
            }
        } else {
            llLoading.setVisibility(android.view.View.GONE);
            llDownload.setVisibility(android.view.View.VISIBLE);
            start.start();
            //为下载完成的回掉做准备
            for (int i = 0; i < bean.getData().size(); i++) {
                if (bean.getData().get(i).getType().equals("1")) {
                    allsize++;
                } else if (bean.getData().get(i).getType().equals("2")) {
                    allsize += 2;
                } else {
                    allsize += bean.getData().get(i).getPhotosets().size();
                }
            }
            //下载全部
            for (int i = 0; i < bean.getData().size(); i++) {
                if (bean.getData().get(i).getType().equals("1")) {
                    String filepath = bean.getData().get(i).getFilepath().replace("10.251.159.9", "218.104.69.90");
                    String gethouzhuiming = FileUtil1.gethouzhuiming(filepath);
                    cutName = DownLoadFileUtils.subFileFullName(bean.getData().get(i).getProgramId() + "", filepath);
                    DownLoadFileUtils.downloadFile(this, filepath, localPath, cutName, filepath, bean.getData().get(i).getProgramId() + "", "1", bean.getData().get(i).getName(), gethouzhuiming, 0, sharedPreferencesUtils, allsize + www, bean.getData().get(i).getDefaultX(), "", "", "", bean, 0, "", "","");
                } else if (bean.getData().get(i).getType().equals("3")) {
                    for (int j = 0; j < bean.getData().get(i).getPhotosets().size(); j++) {
                        localPathPhotoSet = DownLoadFileUtils.customLocalStoragePath("ZHZT/" + "photoset" + bean.getData().get(i).getProgramId());
                        String cutName = DownLoadFileUtils.subFileFullName(bean.getData().get(i).getPhotosets().get(j).getPhotosetId() + "", bean.getData().get(i).getPhotosets().get(j).getFilepath() + "");
                        DownLoadFileUtils.downloadFile(this, bean.getData().get(i).getPhotosets().get(j).getFilepath()+ "", localPathPhotoSet, cutName, bean.getData().get(i).getPhotosets().get(j).getFilepath()+ "", bean.getData().get(i).getProgramId() + "", "3", bean.getData().get(i).getPhotosets().get(j).getName(), "", bean.getData().get(i).getPhotosets().size(), sharedPreferencesUtils, allsize + www, bean.getData().get(i).getDefaultX(), bean.getData().get(i).getName(), bean.getData().get(i).getPlanInterval() + "", bean.getData().get(i).getPhotosets().get(j).getPhotosetId() + "", bean, 0, bean.getData().get(i).getCoverId(), bean.getData().get(i).getPhotosets().size() + "","");
                    }
                } else if (bean.getData().get(i).getType().equals("2")) {
                    localPathVideo = DownLoadFileUtils.customLocalStoragePath("ZHZT/" + "video" + bean.getData().get(i).getProgramId());
                    String cutName = DownLoadFileUtils.subFileFullName("video", bean.getData().get(i).getFilepath());
                    String cutNamecoverimg = DownLoadFileUtils.subFileFullName("coverimg", bean.getData().get(i).getCoverName());
                    DownLoadFileUtils.downloadFile(this, bean.getData().get(i).getFilepath()+ "", localPathVideo, cutName, bean.getData().get(i).getFilepath() + "", bean.getData().get(i).getProgramId() + "", "2", bean.getData().get(i).getName(), "", 0, sharedPreferencesUtils, allsize + www, bean.getData().get(i).getDefaultX(), "", "", "", bean, 0, "", "","");
                    DownLoadFileUtils.downloadFile(this, bean.getData().get(i).getCoverName() + "", localPathVideo, cutNamecoverimg, bean.getData().get(i).getCoverName() + "", bean.getData().get(i).getProgramId() + "", "2", bean.getData().get(i).getName(), "", 0, sharedPreferencesUtils, allsize + www, bean.getData().get(i).getDefaultX(), "", "", "", bean, 0, "", "",bean.getData().get(i).getCoverName());
                }

            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        task.cancel();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getmessage(ConstantEvent event) {
     /*  if (event.getType() == ConstantEvent.ActivityNoticeToDownload) {
            presenter.obtainData("XR4GSLDAIN");
        } else*/
        if (event.getType() == ConstantEvent.Connected) {
            bindSocketService();
            //sp为新建xml文件的文件名，MODE模式，PRIVATE私有
            sharedPreferences = getSharedPreferences("sp", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("connected", "connected");
            editor.commit();
            if (presenter == null) {
                presenter = new PresenterImpl(this);
                presenter.obtainData(getDeviceSN());
            } else {
                presenter.obtainData(getDeviceSN());
            }
        } else if (event.getType() == ConstantEvent.AllDownload) {
            DownLoadFailBean downLoadFailBean = new DownLoadFailBean();
            downLoadFailBean.setFlag("1");
            downLoadFailBean.setMemory("1");
            downLoadFailBean.setMethod("downloadProgram");
            downLoadFailBean.setRealId(getDeviceSN());
            downLoadFailBean.setFailProgramId("");
            Gson gson = new Gson();
            String s1 = gson.toJson(downLoadFailBean);
            socketService.sendOrder(s1);
        } else if (event.getType() == ConstantEvent.FailDownload) {
            String failIds = "";
            List<String> unlineProgramIdList = sharedPreferencesUtils.getDataList("unlineProgramIdList");
            //1.创建两个集合的副本
            //获取线上的programId存入集合
            ArrayList<String> lineProgramIdList = new ArrayList<>();
            for (int i = 0; i < bean.getData().size(); i++) {
                lineProgramIdList.add(bean.getData().get(i).getProgramId() + "");
                Log.i("yyxline", lineProgramIdList.get(i));
            }
            List<String> linefuben = new ArrayList<>(lineProgramIdList);
            List<String> unlinefuben = new ArrayList<>(unlineProgramIdList);
            //2.去除两个集合相同的数据
            linefuben.removeAll(unlineProgramIdList);
            unlinefuben.removeAll(lineProgramIdList);
            for (int i = 0; i < linefuben.size(); i++) {
                failIds += linefuben.get(i) + ",";
            }
            //获取设备剩余内存
            long l = ShengYuCunChuSizeUtil.readSystem();
            Log.i("shengyu", l + "");
            Log.i("shengyu", sum + "");
            if (l < sum) {
                DownLoadFailBean downLoadFailBean = new DownLoadFailBean();
                downLoadFailBean.setFlag("1");
                downLoadFailBean.setMemory("0");
                downLoadFailBean.setMethod("downloadProgram");
                downLoadFailBean.setRealId(getDeviceSN());
                downLoadFailBean.setFailProgramId(failIds);
                Gson gson = new Gson();
                String s1 = gson.toJson(downLoadFailBean);
                socketService.sendOrder(s1);
            } else {
                DownLoadFailBean downLoadFailBean = new DownLoadFailBean();
                downLoadFailBean.setFlag("1");
                downLoadFailBean.setMemory("1");
                downLoadFailBean.setMethod("downloadProgram");
                downLoadFailBean.setRealId(getDeviceSN());
                downLoadFailBean.setFailProgramId(failIds);
                Gson gson = new Gson();
                String s1 = gson.toJson(downLoadFailBean);
                socketService.sendOrder(s1);
            }
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


    public static class DownLoadFileUtils {
        private static String mBasePath; //本地文件存储的完整路径  /storage/emulated/0/book/恰似寒光遇骄阳.txt
        private static List<String> unlineProgramIdList = new ArrayList<>();
        private static int filenum = 0;
        private static int haha = 0;
        private static int i = 0;
        private static int j = 0;
        private static Context context1;
        private static List<PhotoSetNameBean> photonameslist = new ArrayList<>();
        private static List<String> photoSetProgramIdList = new ArrayList<>();
        private static List<String> defaultXlist = new ArrayList<>();
        private static int y = 0;
        private static String type1;
        private static DownloadTask task;
        private static SharedPreferencesUtils sharedPreferencesUtils1;
        private static List<String> downloadfinishProgramIdList = new ArrayList<>();
        private static Bean bean1;
        private static String failIds;

        public static void removeAllDownload() {
//            task.pause();
            sharedPreferencesUtils1.putData("videoProgramId", "");
            filenum = 0;
            task.progress.status = LOADING;
            task.remove();
            //获取线上的programId存入集合
            ArrayList<String> lineProgramIdList = new ArrayList<>();
            for (int i = 0; i < bean1.getData().size(); i++) {
                lineProgramIdList.add(bean1.getData().get(i).getProgramId() + "");
            }
            List<String> linefuben = new ArrayList<>(lineProgramIdList);
            //2.去除两个集合相同的数
            List<SpSaveDefault> listdata1 = sharedPreferencesUtils1.getlistdata("listdata");
            for (int k = 0; k < listdata1.size(); k++) {
                downloadfinishProgramIdList.add(listdata1.get(k).getProgramId() + "");
            }
            linefuben.removeAll(downloadfinishProgramIdList);
            for (int k = 0; k < linefuben.size(); k++) {
                //删除文件
                List<String> filesAllName = FileUtil1.getFilesAllName(Constant.FILEPATH);
                for (int l = 0; l < filesAllName.size(); l++) {
                    if (filesAllName.get(l).contains(linefuben.get(k))) {
                        //删除这个文件夹
                        String s = Constant.FILEPATH + filesAllName.get(l);
                        File file = new File(s);
                        if (file.isFile()) {
                            FileUtil1.deleteFile(s);
                        } else {
                            FileUtil1.deleteDirectory(s);
                        }
                    }
                }
                //删除listdata
                List<SpSaveDefault> listdata = sharedPreferencesUtils1.getlistdata("listdata");
                for (int m = 0; m < listdata.size(); m++) {
                    if (String.valueOf(listdata.get(m).getProgramId()).equals(linefuben.get(k))) {
                        listdata.remove(m);
                    }
                }
                sharedPreferencesUtils1.setlistdata("listdata", listdata);
                //删除unlineprogramIdList
                List<String> unlineProgramIdList = sharedPreferencesUtils1.getDataList("unlineProgramIdList");
                for (int n = 0; n < unlineProgramIdList.size(); n++) {
                    if (unlineProgramIdList.get(n).equals(linefuben.get(k))) {
                        unlineProgramIdList.remove(n);
                    }
                }
                sharedPreferencesUtils1.setDataList("unlineProgramIdList", unlineProgramIdList);
            }

            for (int i = 0; i < linefuben.size(); i++) {
                failIds += linefuben.get(i) + ",";
            }
            DownLoadFailBean downLoadFailBean = new DownLoadFailBean();
            downLoadFailBean.setFlag("1");
            downLoadFailBean.setMemory("1");
            downLoadFailBean.setMethod("downloadProgram");
            downLoadFailBean.setRealId(getDeviceSN());
            String currentfailIds = failIds.replace("null", "");
            downLoadFailBean.setFailProgramId(currentfailIds);
            Gson gson = new Gson();
            String s1 = gson.toJson(downLoadFailBean);
            Log.i("failids", s1);
            socketService.sendOrder(s1);
            failIds = "";
            haha = 0;
        }

        public static void downloadFile(Context context, String fileUrl, String destFileDir, String destFileName, String mFileRelativeUrl, final String programId, final String type, String name, String houzhuiming, final int size, final SharedPreferencesUtils sharedPreferencesUtils, int allsize, String defaultX, String photosetname, String durationtime, String photosetId, Bean bean, int www, String coverId, String photosetsize,String videocoverfileurl) {
            type1 = type;
            bean1 = bean;
            sharedPreferencesUtils1 = sharedPreferencesUtils;
            context1 = context;
            SharedPreferences sharedPreferences = context.getSharedPreferences("onlyonetag", Context.MODE_PRIVATE);
            int onetag = sharedPreferences.getInt("onetag", 0);
            onetag++;
            //步骤1：创建一个SharedPreferences对象
            SharedPreferences sharedPreferences1 = context.getSharedPreferences("onlyonetag", Context.MODE_PRIVATE);
            //步骤2： 实例化SharedPreferences.Editor对象
            SharedPreferences.Editor editor = sharedPreferences1.edit();
            //步骤3：将获取过来的值放入文件
            editor.putInt("onetag", onetag);
            //步骤4：提交
            editor.commit();
            Log.i("onetag", onetag + "");
            GetRequest<File> request = OkGo.<File>get(fileUrl); //构建下载请求
            //创建下载任务，tag为一个任务的唯一标示
            task = OkDownload.request(String.valueOf(onetag), request);
            task.register(new DownloadListener(String.valueOf(onetag)) {
                @Override
                public void onStart(Progress progress) {
                    //开始下载
                    Log.i("aaaaaaaaa", "开始下载文件");
                }

                @Override
                public void onProgress(Progress progress) {
                    Log.i("aaaaaaaaa", "下载文件进度" + progress);
                }

                @Override
                public void onError(Progress progress) {
                    Log.i("aaaaaaaaa", "下载文件出错" + programId);
                    //删除文件
                    File file = new File(Constant.FILEPATH);
                    if (file.exists()) {
                        List<String> filesAllName = FileUtil1.getFilesAllName(Constant.FILEPATH);
                        for (int k = 0; k < filesAllName.size(); k++) {
                            if (filesAllName.get(k).contains(programId)) {
                                //删除这个文件夹
                                FileUtil1.deleteDirectory(Constant.FILEPATH + filesAllName.get(k));
                                lastfilesize = 0;
                            }
                        }
                    }
                }

                @Override
                public void onFinish(File file, Progress progress) {
                    Log.i("aaaaaaaaa", "下载文件完成" + programId);
                    SpSaveDefault spSaveDefault = new SpSaveDefault();
                    if (type.equals("1")) {
                        spSaveDefault.setName(name);
                        spSaveDefault.setProgramId(Integer.parseInt(programId));
                        spSaveDefault.setHouzhuiname(houzhuiming);
                        spSaveDefault.setType(type);
                        spSaveDefault.setDefaultX(defaultX);
                        List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                        listdata.add(spSaveDefault);
                        sharedPreferencesUtils.setlistdata("listdata", listdata);
                        List<String> unlineProgramIdList = sharedPreferencesUtils.getDataList("unlineProgramIdList");
                        unlineProgramIdList.add(programId);
                        sharedPreferencesUtils.setDataList("unlineProgramIdList", unlineProgramIdList);
                    } else if (type.equals("3")) {
                     /*   //将图片及里面图片的名字对应它的id存起来
                        List<PhotoSetNameBean> photosetnamelist1 = sharedPreferencesUtils.getlistdataname("photosetnamelist");
                        if (photosetnamelist1.size() == 0) {
                            PhotoSetNameBean photoSetNameBean = new PhotoSetNameBean();
                            photoSetNameBean.setProgramId(programId);
                            ArrayList<PhotoSetNameBean.DataBean> dataBeans = new ArrayList<>();
                            PhotoSetNameBean.DataBean dataBean = new PhotoSetNameBean.DataBean();
                            dataBean.setPhotosetId(photosetId);
                            dataBean.setName(name);
                            dataBeans.add(dataBean);
                            photoSetNameBean.setDataBeans(dataBeans);
                            //添加到集合，最后保存到sp
                            photonameslist.add(photoSetNameBean);
                            //存入sp
                            sharedPreferencesUtils.setlistdataname("photosetnamelist", photonameslist);
                        } else {
                            for (int k = 0; k < photosetnamelist1.size(); k++) {
                                if (photosetnamelist1.get(k).getProgramId().equals(programId)) {
                                    List<PhotoSetNameBean.DataBean> dataBeans = photosetnamelist1.get(k).getDataBeans();
                                    PhotoSetNameBean.DataBean dataBean = new PhotoSetNameBean.DataBean();
                                    dataBean.setPhotosetId(photosetId);
                                    dataBean.setName(name);
                                    dataBeans.add(dataBean);
                                    photosetnamelist1.get(k).setDataBeans(dataBeans);
                                    //存入sp
                                    sharedPreferencesUtils.setlistdataname("photosetnamelist", photosetnamelist1);
                                } else {
                                    PhotoSetNameBean photoSetNameBean = new PhotoSetNameBean();
                                    photoSetNameBean.setProgramId(programId);
                                    ArrayList<PhotoSetNameBean.DataBean> dataBeans = new ArrayList<>();
                                    PhotoSetNameBean.DataBean dataBean = new PhotoSetNameBean.DataBean();
                                    dataBean.setPhotosetId(photosetId);
                                    dataBean.setName(name);
                                    dataBeans.add(dataBean);
                                    photoSetNameBean.setDataBeans(dataBeans);
                                    //添加到集合，最后保存到sp
                                    photonameslist.add(photoSetNameBean);
                                    //存入sp
                                    sharedPreferencesUtils.setlistdataname("photosetnamelist", photonameslist);
                                }
                            }
                        }*/
                        if (!sharedPreferencesUtils.getData("photosetProgramId", "").equals(programId)) {
                            sharedPreferencesUtils.putData("photosetProgramId", programId);
                            y = 0;
                        }
                        y++;
                        if (String.valueOf(y).equals(photosetsize) && progress.status == FINISH) {
                            spSaveDefault.setName(photosetname);
                            spSaveDefault.setProgramId(Integer.parseInt(programId));
                            spSaveDefault.setType(type);
                            spSaveDefault.setCoverId(coverId);
                            spSaveDefault.setDefaultX(defaultX);
                            List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                            listdata.add(spSaveDefault);
                            sharedPreferencesUtils.setlistdata("listdata", listdata);
                            List<String> unlineProgramIdList = sharedPreferencesUtils.getDataList("unlineProgramIdList");
                            unlineProgramIdList.add(programId);
                            sharedPreferencesUtils.setDataList("unlineProgramIdList", unlineProgramIdList);
                            y = 0;
                        }

                    } else if (type.equals("2")) {
                        if (!sharedPreferencesUtils.getData("videoProgramId", "").equals(programId)) {
                            sharedPreferencesUtils.putData("videoProgramId", programId);
                            j = 0;
                        }
                        j++;

                        if (j == 2 && progress.status == FINISH) {
                            spSaveDefault.setName(name);
                            spSaveDefault.setProgramId(Integer.parseInt(programId));
                            spSaveDefault.setType(type);
                            spSaveDefault.setDefaultX(defaultX);
                            spSaveDefault.setVideocoverurl(videocoverfileurl);
                            List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                            listdata.add(spSaveDefault);
                            sharedPreferencesUtils.setlistdata("listdata", listdata);
                            List<String> unlineProgramIdList = sharedPreferencesUtils.getDataList("unlineProgramIdList");
                            unlineProgramIdList.add(programId);
                            sharedPreferencesUtils.setDataList("unlineProgramIdList", unlineProgramIdList);
                            j = 0;
                        }
                    }

                    haha = 0;
                    //只下载封面的情况
                    //allsize 里面加入要下得封面的个数
                    List<SpSaveDefault> listdata1 = sharedPreferencesUtils.getlistdata("listdata");
                    for (int i = 0; i <bean.getData().size() ; i++) {
                        for (int j = 0; j <listdata1.size() ; j++) {
                            if (listdata1.get(j).getType().equals("2")&&String.valueOf(listdata1.get(j).getProgramId()).equals(bean.getData().get(i).getProgramId()+"")){
                                if (!listdata1.get(j).getVideocoverurl().equals(bean.getData().get(i).getCoverName())){
                                    haha++;
                                }
                            }
                        }
                    }
                    filenum++;
                    if (allsize==0&&filenum==haha){
                        List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                        for (int k = 0; k <listdata.size(); k++) {
                            if (String.valueOf(listdata.get(k).getProgramId()).equals(programId)){
                                listdata.remove(k);
                            }
                        }
                        sharedPreferencesUtils.setlistdata("listdata",listdata);

                        spSaveDefault.setName(name);
                        spSaveDefault.setProgramId(Integer.parseInt(programId));
                        spSaveDefault.setType(type);
                        spSaveDefault.setDefaultX(defaultX);
                        spSaveDefault.setVideocoverurl(videocoverfileurl);
                        List<SpSaveDefault> listdataa = sharedPreferencesUtils.getlistdata("listdata");
                        listdataa.add(spSaveDefault);
                        sharedPreferencesUtils.setlistdata("listdata", listdataa);
                    }
                    //去重listdata
                    List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                    for (int i = 0; i < listdata.size(); i++)  //外循环是循环的次数
                    {
                        for (int j = listdata.size() - 1; j > i; j--)  //内循环是 外循环一次比较的次数
                        {
                            if (listdata.get(i).getProgramId() == listdata.get(j).getProgramId()) {
                                listdata.remove(j);
                            }
                        }
                    }
                    sharedPreferencesUtils.setlistdata("listdata", listdata);
                    Log.i("haha", haha + "");
                    Log.i("haha", filenum + "");
                    Log.i("haha", allsize + "");
                    if (filenum == haha && allsize == 0) {
                        sharedPreferencesUtils.putData("videoProgramId", "");
                        filenum = 0;
                        haha = 0;
                        j = 0;
                        Intent intent = new Intent(context, ListShowActivity.class);
                        context.startActivity(intent);
                        MainActivity mainActivity = (MainActivity) context;
                        mainActivity.finish();
                    }
                    if (filenum == allsize) {
                        filenum = 0;
                        sharedPreferencesUtils.putData("videoProgramId", "");
                        haha = 0;
                        //去重
                        List<SpSaveDefault> listdataa = sharedPreferencesUtils.getlistdata("listdata");
                        for (int i = 0; i < listdataa.size(); i++)  //外循环是循环的次数
                        {
                            for (int j = listdataa.size() - 1; j > i; j--)  //内循环是 外循环一次比较的次数
                            {
                                if (listdataa.get(i).getProgramId() == listdata.get(j).getProgramId()) {
                                    listdataa.remove(j);
                                }
                            }
                        }
                        sharedPreferencesUtils.setlistdata("listdata", listdataa);
                        //unlineProgramIdList去重
                        List<String> unlineProgramIdList1 = sharedPreferencesUtils.getDataList("unlineProgramIdList");
                        for (int i = 0; i < unlineProgramIdList1.size(); i++)  //外循环是循环的次数
                        {
                            for (int j = unlineProgramIdList1.size() - 1; j > i; j--)  //内循环是 外循环一次比较的次数
                            {
                                if (unlineProgramIdList1.get(i).equals(unlineProgramIdList1.get(j))) {
                                    unlineProgramIdList1.remove(j);
                                }
                            }
                        }
                        sharedPreferencesUtils.setDataList("unlineProgramIdList", unlineProgramIdList1);


                        ArrayList<String> strings = new ArrayList<>();
                        for (int i = 0; i < bean.getData().size(); i++) {
                            strings.add(bean.getData().get(i).getDefaultX());
                        }
                        if (strings.contains("1")) {
                            for (int i = 0; i < bean.getData().size(); i++) {
                                if (bean.getData().get(i).getDefaultX().equals("1")) {
                                    String programId = bean.getData().get(i).getProgramId() + "";
                                    String name = bean.getData().get(i).getName();
                                    if (bean.getData().get(i).getType().equals("1")) {
                                        String filepath = bean.getData().get(i).getFilepath();
                                        String gethouzhuiming = FileUtil1.gethouzhuiming(filepath);
                                        Intent intent = new Intent(context, PictureActivity.class);
                                        intent.putExtra("programId", programId);
                                        intent.putExtra("houzhuiname", gethouzhuiming);
                                        intent.putExtra("name", name);
                                        context.startActivity(intent);
                                        MainActivity mainActivity = (MainActivity) context;
                                        mainActivity.finish();
                                    } else if (bean.getData().get(i).getType().equals("2")) {
                                        Intent intent = new Intent(context, VideoActivity.class);
                                        intent.putExtra("programId", programId);
                                        intent.putExtra("name", name);
                                        context.startActivity(intent);
                                        MainActivity mainActivity = (MainActivity) context;
                                        mainActivity.finish();
                                    } else {
                                        Intent intent = new Intent(context, PicturesActivity.class);
                                        intent.putExtra("programId", programId);
                                        context.startActivity(intent);
                                        MainActivity mainActivity = (MainActivity) context;
                                        mainActivity.finish();
                                    }
                                }
                            }
                        } else {
                            Intent intent = new Intent(context, ListShowActivity.class);
                            context.startActivity(intent);
                            MainActivity mainActivity = (MainActivity) context;
                            mainActivity.finish();
                        }
                        //         }

                        sharedPreferencesUtils.setlistdata("listdata", listdata);
                        //全部下载完成
                        EventBus.getDefault().postSticky(new ConstantEvent(10));
                    }
                }

                @Override
                public void onRemove(Progress progress) {

                }
            }).save();
            if (type.equals("1")) {
                task.fileName(destFileName);
            } else if (type.equals("2")) {
                task.folder(Constant.FILEPATH + "video" + programId);
                task.fileName(destFileName);
            } else if (type.equals("3")) {
                task.folder(Constant.FILEPATH + "photoset" + programId);
                task.fileName(destFileName);
            }
            task.fileName(destFileName); //设置下载的文件名
            task.start(); //开始或继续下载
//            task.restart(); //重新下载
//            task.pause(); //暂停下载
//            task.remove(); //删除下载，只删除记录，不删除文件
//            task.remove(true); //删除下载，同时删除记录和文件
        }

        //拼接一个本地的完整的url 供下载文件时传入一个本地的路径
        private static final String mSDPath = Environment.getExternalStorageDirectory().getPath();
        //分类别路径
        private static String mClassifyPath = null;

        public static String customLocalStoragePath(String differentName) {
            File basePath = new File(mSDPath); // /storage/emulated/0
            mClassifyPath = mSDPath + "/" + differentName + "/";  //如果传来的是 book 拼接就是 /storage/emulated/0/book/
            //如果传来的是game  那拼接就是 /storage/emulated/0/game/
            if (!basePath.exists()) {
                basePath.mkdirs();
                System.out.println("文件夹创建成功");
            }
            return mClassifyPath;
        }


        //截取一个文件加载显示时传入的一个本地完整路径
        public static String subFileFullName(String fileName, String fileUrl) {
            String cutName = "";
            if (!fileUrl.equals("")) {
                cutName = fileName + fileUrl.substring(fileUrl.lastIndexOf("."), fileUrl.length());  //这里获取的是  恰似寒光遇骄阳.txt
            }
            return cutName;
        }
    }


}
