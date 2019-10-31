package com.boe.tvdevice.presenter.presenterImpl;

import com.boe.tvdevice.bean.PhotosetInfoBean;
import com.boe.tvdevice.okgo.HttpCommon;
import com.boe.tvdevice.okgo.JsonCallback;
import com.boe.tvdevice.okgo.OkGoUtil;
import com.boe.tvdevice.presenter.PhotosetPresenter;
import com.boe.tvdevice.view.PhotosetView;
import com.lzy.okgo.model.Response;

import java.util.HashMap;

public class PhotosetPresenterImpl implements PhotosetPresenter {
    PhotosetView photosetView;

    public PhotosetPresenterImpl(PhotosetView photosetView) {
        this.photosetView = photosetView;
    }

    @Override
    public void obtainData(String programId) {
        HashMap<String, String> map = new HashMap<>();
        map.put("programId",programId);
        OkGoUtil.postRequestJson(HttpCommon.getDurationTime,this, map,new JsonCallback<PhotosetInfoBean>() {
            @Override
            public void onSuccess(Response<PhotosetInfoBean> response) {
                photosetView.onGetPhotosetInfo(response.body(),null);
            }

            @Override
            public void onError(Response<PhotosetInfoBean> response) {
                photosetView.onGetPhotosetInfo(response.body(),response.message());
            }
        });
    }
}
