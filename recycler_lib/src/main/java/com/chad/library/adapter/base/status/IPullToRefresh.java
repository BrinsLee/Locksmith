package com.chad.library.adapter.base.status;

import androidx.annotation.ColorRes;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


/**
 * Created by flamingo on 2017/12/6.
 */

public interface IPullToRefresh {
    void attachRecyclerView(RecyclerView recyclerView);
    void setColorSchemeResources(@ColorRes int... colorResIds);
    void setRefreshing(boolean isRefreshing);
    boolean isRefreshing();
    void setEnable(boolean isEnable);
    void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener);

}
