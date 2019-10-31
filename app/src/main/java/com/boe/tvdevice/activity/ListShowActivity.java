package com.boe.tvdevice.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;

import androidx.viewpager.widget.ViewPager;

import com.boe.tvdevice.R;
import com.boe.tvdevice.adapter.ProgramAdapter;
import com.boe.tvdevice.base.BaseActivity;
import com.boe.tvdevice.bean.DeleteBean;
import com.boe.tvdevice.bean.SpSaveDefault;
import com.boe.tvdevice.eventBus.ConstantEvent;
import com.boe.tvdevice.presenter.presenterImpl.TellDeletePresenterImpl;
import com.boe.tvdevice.socketKeepLine.SocketService;
import com.boe.tvdevice.utils.SharedPreferencesUtils;
import com.boe.tvdevice.utils.ToastMgr;
import com.boe.tvdevice.view.DeleteView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;

public class ListShowActivity extends BaseActivity implements View.OnClickListener, DeleteView {

    @BindView(R.id.vp_program)
    ViewPager vp_program;
    @BindView(R.id.iv_program_left)
    ImageView iv_program_left;
    @BindView(R.id.iv_program_right)
    ImageView iv_program_right;

    private List<SpSaveDefault> list;
    private ProgramAdapter programAdapter;
    private int allpage = 0;
    private SharedPreferencesUtils sharedPreferencesUtils;
    private SocketService socketService;
    private ServiceConnection sc;
    private String playProgramId;
    private String toPlaySeqServer;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_list_show;
    }

    @Override
    protected int getTitleBarId() {
        return 0;
    }

    @Override
    protected void initView() {
        sharedPreferencesUtils.putData("currentPlayProgramId","");
        //得到总页数
        allpage = list.size() / 8;
        if (list.size() % 8 != 0) {
            allpage = list.size() / 8 + 1;
        }
        programAdapter = new ProgramAdapter(getContext(), list);
        vp_program.setAdapter(programAdapter);
        iv_program_left.setOnClickListener(this);
        iv_program_right.setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ConstantEvent event) {
      if (event.getType()==ConstantEvent.DELETEREFRESH){
            int position = event.getPosition();
            //调接口通知后台
          int currentItem = vp_program.getCurrentItem();
          int realposition=position+currentItem*8;
          TellDeletePresenterImpl tellDeletePresenter = new TellDeletePresenterImpl(this);
            tellDeletePresenter.obtainTellState(getDeviceSN(),String.valueOf(list.get(realposition).getProgramId()));
            if (vp_program.getCurrentItem()==0){
                list.remove(position);
            }else{
                list.remove(position+vp_program.getCurrentItem()*8);
            }

            programAdapter.notifyDataSetChanged();
            if (list.size()==0){
                vp_program.setVisibility(View.GONE);
            }
        }else if (event.getType()==ConstantEvent.NoticeToDownload){
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }else if (event.getType()==ConstantEvent.CloseListShowActivity){
            finish();
        }else if (event.getType()==ConstantEvent.Connected){
            bindSocketService();
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
    protected void initData() {
        EventBus.getDefault().register(this);
        sharedPreferencesUtils = new SharedPreferencesUtils(this);
        list = sharedPreferencesUtils.getlistdata("listdata");
        Collections.reverse(list);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_program_left:
                int currentItem = vp_program.getCurrentItem();
                if (currentItem > 0) {
                    currentItem--;
                }
                vp_program.setCurrentItem(currentItem);
                break;
            case R.id.iv_program_right:
                int currentItem1 = vp_program.getCurrentItem();
                if (currentItem1 < allpage) {
                    currentItem1++;
                }
                vp_program.setCurrentItem(currentItem1);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onGetDeleteState(DeleteBean deleteBean, String errorMsg) {
        if (deleteBean.getState().equals("s")){
            ToastMgr.show("删除成功");
        }else {
            ToastMgr.show("删除通知后台失败");
        }
    }
}
