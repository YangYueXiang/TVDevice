package com.boe.tvdevice.okgo;

import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;

import java.util.Map;

/**
 * Created by ccb on 2017/10/12.
 * 网络框架二次封装
 */

public class OkGoUtil {
    /**
     * 上传json数据 并携带请求头
     * SN:        扫描枪唯一设备识别号在请求头中永远携带
     * userId:    操作员id
     * type:      设备归属 1-行哲 2-彩娱 3-华智融 4-世麦智能 5-国微 6-证通 7-新大陆 8-艾体威尔 9-优博讯 10-升腾
     */
    public static <T> void postRequestJson(String url, Object tag, Map<String, String> params, JsonCallback<T> callback) {
        // TODO: 2017/10/13  加密 时间戳等 请求日志打印
        Log.d("OkGoUtil", "method post");
        HttpParams httpParams = new HttpParams();
        httpParams.put(params);
//        JSONObject jsonObject = new JSONObject(params);
        OkGo.<T>post(url)
                .tag(tag)
                //      .headers("exhibitsRealId",sn)
                //       .headers("userId",userId)
                //       .headers("type",type)
//                .upJson(jsonObject)
                .params(httpParams)
                .execute(callback);
    }

    }
