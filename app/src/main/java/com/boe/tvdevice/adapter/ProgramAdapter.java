package com.boe.tvdevice.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.boe.tvdevice.R;
import com.boe.tvdevice.bean.SpSaveDefault;
import com.boe.tvdevice.customView.MyGridView;

import java.util.ArrayList;
import java.util.List;

public class ProgramAdapter extends PagerAdapter {
    private Context context;
    private List<SpSaveDefault> list;
    private GridProgramAdapter gridProgramAdapter;
    private int mChildCount = 0;

    public ProgramAdapter(Context context, List<SpSaveDefault> list) {
        this.context = context;
        this.list = list;
    }

    @Override public void notifyDataSetChanged() {
        mChildCount = getCount();
        super.notifyDataSetChanged();
    }

    @Override public int getItemPosition(Object object) {
        if (mChildCount > 0) {
            mChildCount--;
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    @Override
    public int getCount() {
        final int count = (int) Math.ceil(list.size() / 8.0f);
        return count;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View inflate = View.inflate(context, R.layout.view_vp_program_item, null);
        int start = 0;
        int end = 0;
        int y = 1;
        MyGridView gvProgram = inflate.findViewById(R.id.gv_program);

        start = (position * 8);
        end = ((position * 8) + 8);

        List<SpSaveDefault> pageList = new ArrayList<>();
        for (int i = start; i < end && i < list.size(); i++) {
            pageList.add(list.get(i));
        }
        gridProgramAdapter = new GridProgramAdapter(context, pageList);
        gvProgram.setAdapter(gridProgramAdapter);
        container.addView(inflate);
        return inflate;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    public  GridProgramAdapter getGridProgramAdapter(){
        return gridProgramAdapter;
    }
}
