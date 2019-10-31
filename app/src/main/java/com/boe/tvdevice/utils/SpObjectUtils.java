package com.boe.tvdevice.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SpObjectUtils {
    /**
     * 把对象保存到以String形式保存到sp中
     * @param context 上下文
     * @param t 泛型参数
     * @param spName sp文件名
     * @param keyName 字段名
     * @param
     */
    public static void saveObj2SP(Context context, Object t, String spName, String keyName) {

        SharedPreferences preferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        ByteArrayOutputStream bos;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(t);
            byte[] bytes = bos.toByteArray();
// String ObjStr = new String(bytes,0,bytes.length);
            String ObjStr = Base64.encodeToString(bytes, Base64.DEFAULT);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(keyName, ObjStr);
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//关闭流
            if (oos != null) {
                try {
                    oos.flush();
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

    }

    /**
     * sp文件名 和 字段名相同
     * @param context
     * @param t
     * @param spName
     * @param <T>
     */
    public static <T> void saveObj2SP(Context context, T t, String spName) {
        saveObj2SP(context, t, spName, spName);
    }


    /**
     *从sp中读取对象
     * @param context
     * @param spName sp文件名
     * @param keyNme 字段名
     * @param <T>    泛型参数
     * @return
     */
    public static <T extends Object> T getObjFromSp(Context context, String spName, String keyNme) {
        SharedPreferences preferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        byte[] bytes = Base64.decode(preferences.getString(keyNme, ""), Base64.DEFAULT);
        ByteArrayInputStream bis;
        ObjectInputStream ois = null;
        T obj = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = (T) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }

    public static <T extends Object> T getObjFromSp(Context context, String spName){
        return  getObjFromSp(context,spName,spName);
    }
}
