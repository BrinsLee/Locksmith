package com.chad.library.adapter.base.status;

import com.chad.library.R;


/**
 * Created by flamingo on 2017/12/6.
 */

public class SimpleStatusView extends StatusView {

    @Override
    protected void onBindState() {
        bindState(STATUS_LOAD_FAILED, R.id.load_filed);
        bindState(STATUS_LOADING, R.id.load_loading);
        bindState(STATUS_NO_DATA, R.id.load_no_data);
        bindState(STATUS_NO_NET, R.id.load_no_net);
    }

    @Override
    public int getLayoutId() {
        return R.layout.quick_view_load_status;
    }

}
