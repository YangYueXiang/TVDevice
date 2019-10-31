package com.boe.tvdevice.presenter.presenterImpl;

import com.boe.tvdevice.view.View;

import com.boe.tvdevice.bean.Bean;
import com.boe.tvdevice.okgo.HttpCommon;
import com.boe.tvdevice.okgo.JsonCallback;
import com.boe.tvdevice.okgo.OkGoUtil;
import com.boe.tvdevice.presenter.Presenter;
import com.lzy.okgo.model.Response;

import java.util.HashMap;

public class PresenterImpl implements Presenter {
    View view;

    public PresenterImpl(View view) {
        this.view = view;
    }

    @Override
    public void obtainData(String exhibitsRealId) {
        HashMap<String, String> map = new HashMap<>();
        map.put("exhibitsRealId",exhibitsRealId);
        OkGoUtil.postRequestJson(HttpCommon.getData,this, map,new JsonCallback<Bean>() {
            @Override
            public void onSuccess(Response<Bean> response) {
                view.onGetData(response.body(),null);
            }

            @Override
            public void onError(Response<Bean> response) {
                view.onGetData(response.body(),response.message());
            }
        });
    }
}
