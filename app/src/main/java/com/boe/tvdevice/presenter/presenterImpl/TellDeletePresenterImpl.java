package com.boe.tvdevice.presenter.presenterImpl;

import com.boe.tvdevice.bean.DeleteBean;
import com.boe.tvdevice.okgo.HttpCommon;
import com.boe.tvdevice.okgo.JsonCallback;
import com.boe.tvdevice.okgo.OkGoUtil;
import com.boe.tvdevice.presenter.TellDeletePresenter;
import com.boe.tvdevice.view.DeleteView;
import com.lzy.okgo.model.Response;

import java.util.HashMap;

public class TellDeletePresenterImpl implements TellDeletePresenter {
    DeleteView deleteView;

    public TellDeletePresenterImpl(DeleteView deleteView) {
        this.deleteView = deleteView;
    }

    @Override
    public void obtainTellState(String exhibitsRealId, String programId) {
        HashMap<String, String> map = new HashMap<>();
        map.put("exhibitsRealId",exhibitsRealId);
        map.put("programId",programId);
        OkGoUtil.postRequestJson(HttpCommon.tellDelete,this, map,new JsonCallback<DeleteBean>() {
            @Override
            public void onSuccess(Response<DeleteBean> response) {
                deleteView.onGetDeleteState(response.body(),null);
            }

            @Override
            public void onError(Response<DeleteBean> response) {
                deleteView.onGetDeleteState(response.body(),response.message());
            }
        });
    }
}
