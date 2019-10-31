package com.boe.tvdevice.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.boe.tvdevice.bean.PhotoSetNameBean;
import com.boe.tvdevice.bean.SpSaveDefault;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;


/**
 * Explain:
 * Author: lp
 * Create: 2018/5/29 14:20
 * Version: 1.0
 */

public class SharedPreferencesUtils {

    private static SharedPreferences sharedPreferences;

    private static SharedPreferences.Editor editor;
    public SharedPreferencesUtils(Context context) {
        sharedPreferences = context.getSharedPreferences("share_data", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static void setlistdata(String key,List<SpSaveDefault> list){
        Gson gson = new Gson();
        String jsonStr=gson.toJson(list); //将List转换成Json
        editor.putString(key, jsonStr) ; //存入json串
        editor.commit() ;  //提交
    }

    public List<SpSaveDefault> getlistdata(String key){
        ArrayList<SpSaveDefault> list = new ArrayList<>();
        String peopleListJson = sharedPreferences.getString(key,"");  //取出key为"KEY_PEOPLE_DATA"的值，如果值为空，则将第二个参数作为默认值赋值
        if(peopleListJson!="")  //防空判断
        {
            Gson gson = new Gson();
          list = gson.fromJson(peopleListJson,new TypeToken<List<SpSaveDefault>>(){}.getType());
        }
        return list;
    }

    public static void setlistdataname(String key,List<PhotoSetNameBean> list){
        Gson gson = new Gson();
        String jsonStr=gson.toJson(list); //将List转换成Json
        editor.putString(key, jsonStr) ; //存入json串
        editor.commit() ;  //提交
    }

    public List<PhotoSetNameBean> getlistdataname(String key){
        ArrayList<PhotoSetNameBean> list = new ArrayList<>();
        String peopleListJson = sharedPreferences.getString(key,"");  //取出key为"KEY_PEOPLE_DATA"的值，如果值为空，则将第二个参数作为默认值赋值
        if(peopleListJson!="")  //防空判断
        {
            Gson gson = new Gson();
            list = gson.fromJson(peopleListJson,new TypeToken<List<PhotoSetNameBean>>(){}.getType());
        }
        return list;
    }

    /**
     * 保存数据的方法，拿到数据保存数据的基本类型，然后根据类型调用不同的保存方法
     * @param key
     * @param object
     */
    public static void putData(String key, Object object) {

        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.commit();
    }

    /**
     * 获取保存数据的方法，我们根据默认值的到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key           键的值
     * @param defaultObject 默认值
     * @return
     */
    public static Object getData(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return sharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sharedPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sharedPreferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sharedPreferences.getLong(key, (Long) defaultObject);
        } else {
            return sharedPreferences.getString(key, null);
        }
    }



    /**
     * 保存List
     * @param tag
     * @param datalist
     */
    public static void setDataList(String tag, List<String> datalist) {
        if (null == datalist || datalist.size() <= 0)
            return;
        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
//        editor.clear();
        editor.putString(tag, strJson);
        editor.commit();

    }

    /**
     * 获取List
     * @param tag
     * @return
     */
    public static List<String> getDataList(String tag) {
        List<String> datalist=new ArrayList<String>();
        String strJson ="";
        try {
            strJson = sharedPreferences.getString(tag, null);
        }catch (Exception e){
            Log.e("","");
        }
//        String strJson = sharedPreferences.getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<String>>() {
        }.getType());
        return datalist;

    }

   public static void clean(Context context){
       context.getSharedPreferences("share_data", Context.MODE_PRIVATE).edit().clear().commit();
   }



}
