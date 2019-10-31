package com.boe.tvdevice.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boe.tvdevice.R;
import com.boe.tvdevice.activity.PictureActivity;
import com.boe.tvdevice.activity.PicturesActivity;
import com.boe.tvdevice.activity.VideoActivity;
import com.boe.tvdevice.bean.PhotoSetNameBean;
import com.boe.tvdevice.bean.SpSaveDefault;
import com.boe.tvdevice.bean.constant.Constant;
import com.boe.tvdevice.dialog.DeleteDialog;
import com.boe.tvdevice.eventBus.ConstantEvent;
import com.boe.tvdevice.utils.FileUtil1;
import com.boe.tvdevice.utils.LoadLocalImageUtils;
import com.boe.tvdevice.utils.SharedPreferencesUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

public class GridProgramAdapter extends BaseAdapter {
    private Context context;
    private List<SpSaveDefault> list;
    private int i;
    private boolean showState = false;
    private final SharedPreferencesUtils sharedPreferencesUtils;


    public GridProgramAdapter(Context context, List<SpSaveDefault> list) {
        this.context = context;
        this.list = list;
        sharedPreferencesUtils = new SharedPreferencesUtils(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("deleteafter",list.toString());
        Log.i("deleteafter",list.get(position).getType().toString());
        Log.i("deleteafter",list.get(position).getProgramId()+"");
        SpSaveDefault spSaveDefault = list.get(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            String type = spSaveDefault.getType();
            i = Integer.parseInt(type);
            switch (i) {
                case 1:
                    convertView = View.inflate(context, R.layout.view_gv_picture_item, null);
                    holder.tvFileName = convertView.findViewById(R.id.tv_picture_name);
                    holder.ivFilePicture = convertView.findViewById(R.id.iv_picture);
                    holder.ll_pictureplay_or_delete=convertView.findViewById(R.id.ll_pictureplay_or_delete);
                    holder.iv_pictureplay=convertView.findViewById(R.id.iv_pictureplay);
                    holder.iv_picturedelete=convertView.findViewById(R.id.iv_picturedelete);
                    break;
                case 2:
                    convertView = View.inflate(context, R.layout.view_gv_video_item, null);
                    holder.tvFileName = convertView.findViewById(R.id.tv_video_name);
                    holder.ivFilePicture = convertView.findViewById(R.id.iv_video_picture);
                    holder.ll_videoplay_or_delete=convertView.findViewById(R.id.ll_videoplay_or_delete);
                    holder.iv_videoplay=convertView.findViewById(R.id.iv_videoplay);
                    holder.iv_videodelete=convertView.findViewById(R.id.iv_videodelete);
                    break;
                case 3:
                    convertView = View.inflate(context, R.layout.view_gv_pictures_item, null);
                    holder.tvFileName = convertView.findViewById(R.id.tv_pictures_name);
                    holder.ivFilePicture = convertView.findViewById(R.id.iv_pictures_one);
                    holder.ll_picturesplay_or_delete=convertView.findViewById(R.id.ll_picturesplay_or_delete);
                    holder.iv_picturesplay=convertView.findViewById(R.id.iv_picturesplay);
                    holder.iv_picturesdelete=convertView.findViewById(R.id.iv_picturesdelete);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        switch (i) {
            case 1:
                holder.tvFileName.setText(spSaveDefault.getName());
                int programId = spSaveDefault.getProgramId();
                String houzhuiname = spSaveDefault.getHouzhuiname();
                String s = String.valueOf(programId);
                String url =Constant.FILEPATH + s + houzhuiname;
                Bitmap bitmap = LoadLocalImageUtils.getLoacalBitmap(url);
                holder.ivFilePicture.setImageBitmap(bitmap);
                holder.ivFilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (showState) {
                            holder.ll_pictureplay_or_delete.setVisibility(View.GONE);
                            showState = false;
                        }else{
                            holder.ll_pictureplay_or_delete.setVisibility(View.VISIBLE);
                            showState = true;
                        }
                    }
                });
                holder.iv_pictureplay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, PictureActivity.class);
                        intent.putExtra("programId", spSaveDefault.getProgramId() + "");
                        intent.putExtra("houzhuiname", spSaveDefault.getHouzhuiname());
                        intent.putExtra("name", spSaveDefault.getName());
                        context.startActivity(intent);
                        EventBus.getDefault().postSticky(new ConstantEvent(8));
                    }
                });
                holder.iv_picturedelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DeleteDialog(context).builder()
                                .setMsg("确认删除"+spSaveDefault.getName()+"节目？")
                                .setPositiveButton("删除", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        //从本地把这个节目删除
                                        List<String> filesAllName = FileUtil1.getFilesAllName(Constant.FILEPATH);
                                        for (int i = 0; i <filesAllName.size() ; i++) {
                                            if (filesAllName.get(i).contains(spSaveDefault.getProgramId()+"")){
                                                File file1 = new File(Constant.FILEPATH + filesAllName.get(i));
                                                if (file1.isFile()) {
                                                    FileUtil1.deleteFile(Constant.FILEPATH + filesAllName.get(i));
                                                } else {
                                                    FileUtil1.deleteDirectory(Constant.FILEPATH + filesAllName.get(i));
                                                }
                                            }
                                        }

                                        //把listdata  sp里面的数据同步减少
                                        List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                                        for (int k = 0; k < listdata.size(); k++) {
                                            if (String.valueOf(listdata.get(k).getProgramId()).equals(spSaveDefault.getProgramId()+"")) {
                                                listdata.remove(listdata.get(k));
                                            }
                                        }
                                        sharedPreferencesUtils.setlistdata("listdata", listdata);

                                        //unlineProgramId  sp数据同样同步减少
                                        List<String> unlineProgramIdList1 = sharedPreferencesUtils.getDataList("unlineProgramIdList");
                                        for (int j = 0; j < unlineProgramIdList1.size(); j++) {
                                            if (String.valueOf(spSaveDefault.getProgramId()).equals(unlineProgramIdList1.get(j))) {
                                                unlineProgramIdList1.remove(unlineProgramIdList1.get(j));
                                            }
                                        }
                                        sharedPreferencesUtils.setDataList("unlineProgramIdList", unlineProgramIdList1);

                                        //photonameslist sp 同样去掉
                                        List<PhotoSetNameBean> photosetnamelist = sharedPreferencesUtils.getlistdataname("photosetnamelist");
                                        for (int l = 0; l < photosetnamelist.size(); l++) {
                                            if (photosetnamelist.get(l).getProgramId().equals(spSaveDefault.getProgramId())) {
                                                photosetnamelist.remove(photosetnamelist.get(l));
                                            }
                                        }
                                        sharedPreferencesUtils.setlistdataname("photosetnamelist", photosetnamelist);
                                        ConstantEvent constantEvent = new ConstantEvent(3);
                                        constantEvent.setPosition(position);
                                        //Eventbus通知刷新适配器
                                        EventBus.getDefault().postSticky(constantEvent);
                                    }
                                }).setNegativeButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                    }
                });
                break;
            case 2:
                String coverpath = null;
                int programId1 = spSaveDefault.getProgramId();
                String url1 =Constant.FILEPATH + "video" + programId1;
                //获取这个文件夹下面所有文件名字
                File file = new File(url1);
                if (file.exists()){
                    List<String> filesAllName = FileUtil1.getFilesAllName(url1);
                    if (filesAllName.size()>0){
                        for (int s1 = 0; s1 < filesAllName.size(); s1++) {
                            if (!filesAllName.get(s1).contains("mp4") && !filesAllName.get(s1).contains("mov")) {
                                //对比coverimg后面名字，谁大用谁
                                coverpath =Constant.FILEPATH + "video" + programId1 + "/" + filesAllName.get(s1);
                            }
                        }
                        Bitmap bitmap1 = LoadLocalImageUtils.getLoacalBitmap(coverpath);
                        holder.tvFileName.setText(spSaveDefault.getName());
                        holder.ivFilePicture.setImageBitmap(bitmap1);
                    }
                }


                //点击显示播放删除的按钮
                holder.ivFilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (showState) {
                            holder.ll_videoplay_or_delete.setVisibility(View.GONE);
                            showState = false;
                        }else{
                            holder.ll_videoplay_or_delete.setVisibility(View.VISIBLE);
                            showState = true;
                        }
                    }
                });

                holder.iv_videoplay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, VideoActivity.class);
                        intent.putExtra("programId", spSaveDefault.getProgramId() + "");
                        intent.putExtra("name", spSaveDefault.getName());
                        context.startActivity(intent);
                        EventBus.getDefault().postSticky(new ConstantEvent(8));
                    }
                });
                holder.iv_videodelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DeleteDialog(context).builder()
                                .setMsg("确认删除"+spSaveDefault.getName()+"节目？")
                                .setPositiveButton("删除", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        //从本地把这个节目删除
                                        List<String> filesAllName = FileUtil1.getFilesAllName(Constant.FILEPATH);
                                        for (int i = 0; i <filesAllName.size() ; i++) {
                                            if (filesAllName.get(i).contains(spSaveDefault.getProgramId()+"")){
                                                File file1 = new File(Constant.FILEPATH + filesAllName.get(i));
                                                if (file1.isFile()) {
                                                    FileUtil1.deleteFile(Constant.FILEPATH + filesAllName.get(i));
                                                } else {
                                                    FileUtil1.deleteDirectory(Constant.FILEPATH + filesAllName.get(i));
                                                }
                                            }
                                        }

                                        //把listdata  sp里面的数据同步减少
                                        List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                                        for (int k = 0; k < listdata.size(); k++) {
                                            if (String.valueOf(listdata.get(k).getProgramId()).equals(spSaveDefault.getProgramId()+"")) {
                                                listdata.remove(listdata.get(k));
                                            }
                                        }
                                        sharedPreferencesUtils.setlistdata("listdata", listdata);

                                        //unlineProgramId  sp数据同样同步减少
                                        List<String> unlineProgramIdList1 = sharedPreferencesUtils.getDataList("unlineProgramIdList");
                                        for (int j = 0; j < unlineProgramIdList1.size(); j++) {
                                            if (String.valueOf(spSaveDefault.getProgramId()).equals(unlineProgramIdList1.get(j))) {
                                                unlineProgramIdList1.remove(unlineProgramIdList1.get(j));
                                            }
                                        }
                                        sharedPreferencesUtils.setDataList("unlineProgramIdList", unlineProgramIdList1);
                                        Log.i("deleteafter",listdata.toString());
                                        Log.i("deleteafter",unlineProgramIdList1.toString());
                                      /*  //photonameslist sp 同样去掉
                                        List<PhotoSetNameBean> photosetnamelist = sharedPreferencesUtils.getlistdataname("photosetnamelist");
                                        for (int l = 0; l < photosetnamelist.size(); l++) {
                                            if (photosetnamelist.get(l).getProgramId().equals(spSaveDefault.getProgramId())) {
                                                photosetnamelist.remove(photosetnamelist.get(l));
                                            }
                                        }
                                        sharedPreferencesUtils.setlistdataname("photosetnamelist", photosetnamelist);*/
                                        ConstantEvent constantEvent = new ConstantEvent(3);
                                        constantEvent.setPosition(position);
                                        //Eventbus通知刷新适配器
                                        EventBus.getDefault().postSticky(constantEvent);
                                    }
                                }).setNegativeButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                    }
                });
                break;
            case 3:
                int programId2 = spSaveDefault.getProgramId();
                String url2 = Constant.FILEPATH + "photoset" + programId2;
                //获取这个文件夹下面所有文件名字
                String coverId = spSaveDefault.getCoverId();
                Log.i("coverId",coverId);
                List<String> filesAllName2 = FileUtil1.getFilesAllName(url2);
                for (int j = 0; j <filesAllName2.size() ; j++) {
                    if (filesAllName2.get(j).contains(coverId)){
                        String path =Constant.FILEPATH + "photoset" + programId2 + "/" + filesAllName2.get(j);
                        Bitmap bitmap2 = LoadLocalImageUtils.getLoacalBitmap(path);
                        holder.tvFileName.setText(spSaveDefault.getName());
                        holder.ivFilePicture.setImageBitmap(bitmap2);
                    }
                }


                //点击显示播放删除的按钮
                holder.ivFilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (showState) {
                            holder.ll_picturesplay_or_delete.setVisibility(View.GONE);
                            showState = false;
                        }else{
                            holder.ll_picturesplay_or_delete.setVisibility(View.VISIBLE);
                            showState = true;
                        }
                    }
                });
                holder.iv_picturesplay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, PicturesActivity.class);
                        intent.putExtra("programId", spSaveDefault.getProgramId() + "");
                        context.startActivity(intent);
                    //    EventBus.getDefault().postSticky(new ConstantEvent(8));
                    }
                });
                holder.iv_picturesdelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new DeleteDialog(context).builder()
                                    .setMsg("确认删除"+spSaveDefault.getName()+"节目？")
                                    .setPositiveButton("删除", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //从本地把这个节目删除
                                            List<String> filesAllName = FileUtil1.getFilesAllName(Constant.FILEPATH);
                                            for (int i = 0; i <filesAllName.size() ; i++) {
                                                if (filesAllName.get(i).contains(spSaveDefault.getProgramId()+"")){
                                                    File file1 = new File(Constant.FILEPATH + filesAllName.get(i));
                                                    if (file1.isFile()) {
                                                        FileUtil1.deleteFile(Constant.FILEPATH + filesAllName.get(i));
                                                    } else {
                                                        FileUtil1.deleteDirectory(Constant.FILEPATH + filesAllName.get(i));
                                                    }
                                                }
                                            }

                                            //把listdata  sp里面的数据同步减少
                                            List<SpSaveDefault> listdata = sharedPreferencesUtils.getlistdata("listdata");
                                            for (int k = 0; k < listdata.size(); k++) {
                                                if (String.valueOf(listdata.get(k).getProgramId()).equals(spSaveDefault.getProgramId()+"")) {
                                                    listdata.remove(listdata.get(k));
                                                }
                                            }
                                            sharedPreferencesUtils.setlistdata("listdata", listdata);

                                            //unlineProgramId  sp数据同样同步减少
                                            List<String> unlineProgramIdList1 = sharedPreferencesUtils.getDataList("unlineProgramIdList");
                                            for (int j = 0; j < unlineProgramIdList1.size(); j++) {
                                                if (String.valueOf(spSaveDefault.getProgramId()).equals(unlineProgramIdList1.get(j))) {
                                                    unlineProgramIdList1.remove(unlineProgramIdList1.get(j));
                                                }
                                            }
                                            sharedPreferencesUtils.setDataList("unlineProgramIdList", unlineProgramIdList1);

                                            //photonameslist sp 同样去掉
                                            List<PhotoSetNameBean> photosetnamelist = sharedPreferencesUtils.getlistdataname("photosetnamelist");
                                            for (int l = 0; l < photosetnamelist.size(); l++) {
                                                if (photosetnamelist.get(l).getProgramId().equals(spSaveDefault.getProgramId())) {
                                                    photosetnamelist.remove(photosetnamelist.get(l));
                                                }
                                            }
                                            sharedPreferencesUtils.setlistdataname("photosetnamelist", photosetnamelist);
                                            ConstantEvent constantEvent = new ConstantEvent(3);
                                            constantEvent.setPosition(position);
                                            //Eventbus通知刷新适配器
                                            EventBus.getDefault().postSticky(constantEvent);
                                        }
                                    }).setNegativeButton("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                        }
                });
                break;
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvFileName;
        ImageView ivFilePicture;
        LinearLayout iv_videoplay;
        LinearLayout iv_videodelete;
        LinearLayout iv_pictureplay;
        LinearLayout iv_picturedelete;
        LinearLayout iv_picturesplay;
        LinearLayout iv_picturesdelete;
        LinearLayout ll_pictureplay_or_delete;
        LinearLayout ll_videoplay_or_delete;
        LinearLayout ll_picturesplay_or_delete;
    }

}
