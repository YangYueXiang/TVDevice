package com.boe.tvdevice.view;

import com.boe.tvdevice.bean.Bean;

public interface View {
    void onGetData(Bean bean, String errorMsg);
}
