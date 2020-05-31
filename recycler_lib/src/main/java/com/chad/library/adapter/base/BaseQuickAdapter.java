/*
 * Copyright 2013 Joan Zapata
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chad.library.adapter.base;

import android.animation.Animator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.chad.library.adapter.base.animation.BaseAnimation;
import com.chad.library.adapter.base.animation.ScaleInAnimation;
import com.chad.library.adapter.base.animation.SlideInBottomAnimation;
import com.chad.library.adapter.base.animation.SlideInLeftAnimation;
import com.chad.library.adapter.base.animation.SlideInRightAnimation;
import com.chad.library.adapter.base.loadmore.LoadMoreView;
import com.chad.library.adapter.base.loadmore.SimpleLoadMoreView;
import com.chad.library.adapter.base.model.BaseCollection;
import com.chad.library.adapter.base.model.BaseData;
import com.chad.library.adapter.base.model.IExpandable;
import com.chad.library.adapter.base.status.IPullToRefresh;
import com.chad.library.adapter.base.status.SimpleStatusView;
import com.chad.library.adapter.base.status.StatusView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public abstract class BaseQuickAdapter<T extends BaseData, K extends BaseViewHolder> extends RecyclerView.Adapter<K> {
    public static final int DEFAULT_LOAD_DATA_COUNT_PER_REQUEST = 15;
    public static final int SMALLEST_SLIDING_DISTANCE = 10;

    boolean mUserDefineEnableLoadMore = true;
    private boolean mUserDefineEnableRefresh = true;

    // onBindViewHolder检测到ViewType为LOADING_VIEW的loading界面时，禁止autoLoadMore()的标记位
    boolean mManuallyLoadMoreEnable = false;
    private boolean mResetManuallyLoadMoreEnable = false;

    //load more
    private boolean mLoadMoreEnable = false;
    private boolean mLoading = false;
    private LoadMoreView mLoadMoreView = new SimpleLoadMoreView();
    private boolean mEnableLoadMoreEndClick = false;

    private boolean mIsAutoPreLoad = true;

    //Animation
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int ALPHA_IN = 0x00000001;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SCALE_IN = 0x00000002;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDE_IN_BOTTOM = 0x00000003;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDE_IN_LEFT = 0x00000004;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDE_IN_RIGHT = 0x00000005;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemChildClickListener mOnItemChildClickListener;
    private OnItemChildLongClickListener mOnItemChildLongClickListener;
    private OnLoadDataListener<T> mOnLoadDataListener;
    private int mLoadDataCountPerRequest = DEFAULT_LOAD_DATA_COUNT_PER_REQUEST;

    @IntDef({ALPHA_IN, SCALE_IN, SLIDE_IN_BOTTOM, SLIDE_IN_LEFT, SLIDE_IN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {
    }

    private boolean mFirstOnlyEnable = true;
    private boolean mOpenAnimationEnable = false;
    private Interpolator mInterpolator = new LinearInterpolator();
    private int mDuration = 300;
    private int mLastPosition = -1;

    private BaseAnimation mCustomAnimation;
    private BaseAnimation mSelectAnimation = new AlphaInAnimation();
    //header footer
    private LinearLayout mHeaderLayout;
    private LinearLayout mFooterLayout;
    //Status
    private FrameLayout mStatusLayout;
    private StatusView mStatusView;
    private boolean mHeadAndStatusEnable;
    private boolean mFootAndStatusEnable;

//    private String mNoNetText = "网络异常，请稍后再试";

    //下拉刷新接口
    private IPullToRefresh mRefresh;

    protected static final String TAG = BaseQuickAdapter.class.getSimpleName();
    protected Context mContext;
    protected LayoutInflater mLayoutInflater;
    protected BaseCollection<T> mData;
    public static final int HEADER_VIEW = -10000;
    public static final int LOADING_VIEW = -10001;
    public static final int FOOTER_VIEW = -10002;
    public static final int STATE_VIEW = -10003;
    public static final int EMPTY_VIEW = -10004; // 一个empty的item

    private RecyclerView mRecyclerView;
    private int mViewType;
    private Handler mHandler;


    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            setEnableRefreshInternal(mUserDefineEnableRefresh);
            if (mData.isEmpty()) {
                mStatusView.showStatus(StatusView.STATUS_NO_DATA);
                setEnableLoadMoreInternal(false);
            } else if (mData.getRealSize() != 0 && mData.getRealSize() % mLoadDataCountPerRequest == 0) {
                setEnableLoadMoreInternal(mUserDefineEnableLoadMore);
            } else {
                setEnableLoadMoreInternal(false);
            }
        }
    };


    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    private void checkNotNull() {
        if (getRecyclerView() == null) {
            throw new RuntimeException("please bind recyclerView first!");
        }
    }

    /**
     * @see #disableLoadMoreIfNotFullPage(RecyclerView)
     */
    public void disableLoadMoreIfNotFullPage() {
        checkNotNull();
        disableLoadMoreIfNotFullPage(getRecyclerView());
    }

    /**
     * check if full page after {@link #setNewData(List)}, if full, it will enable load more again.
     * <p>
     * 不是配置项！！
     * <p>
     * 这个方法是用来检查是否满一屏的，所以只推荐在 {@link #setNewData(List)} 之后使用
     * 原理很简单，先关闭 load more，检查完了再决定是否开启
     * <p>
     * 不是配置项！！
     *
     * @param recyclerView your recyclerView
     * @see #setNewData(List)
     */
    public void disableLoadMoreIfNotFullPage(RecyclerView recyclerView) {
        setEnableLoadMoreInternal(false);
        if (recyclerView == null) return;
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager == null) return;
        if (manager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) manager;
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ((linearLayoutManager.findLastCompletelyVisibleItemPosition() + 1) != getItemCount()) {
                        setEnableLoadMoreInternal(true);
                    }
                }
            }, 50);
        } else if (manager instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) manager;
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final int[] positions = new int[staggeredGridLayoutManager.getSpanCount()];
                    staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(positions);
                    int pos = getTheBiggestNumber(positions) + 1;
                    if (pos != getItemCount()) {
                        setEnableLoadMoreInternal(true);
                    }
                }
            }, 50);
        }
    }

    private int getTheBiggestNumber(int[] numbers) {
        int tmp = -1;
        if (numbers == null || numbers.length == 0) {
            return tmp;
        }
        for (int num : numbers) {
            if (num > tmp) {
                tmp = num;
            }
        }
        return tmp;
    }

    /**
     * up fetch start
     */
    private boolean mUpFetchEnable;
    private boolean mUpFetching;
    private UpFetchListener mUpFetchListener;

    public void setUpFetchEnable(boolean upFetch) {
        this.mUpFetchEnable = upFetch;
    }

    public boolean isUpFetchEnable() {
        return mUpFetchEnable;
    }

    /**
     * start up fetch position, default is 1.
     */
    private int mStartUpFetchPosition = 1;

    public void setStartUpFetchPosition(int startUpFetchPosition) {
        mStartUpFetchPosition = startUpFetchPosition;
    }

    private void autoUpFetch(int positions) {
        if (!isUpFetchEnable() || isUpFetching()) {
            return;
        }
        if (positions <= mStartUpFetchPosition && mUpFetchListener != null) {
            mUpFetchListener.onUpFetch();
        }
    }

    public boolean isUpFetching() {
        return mUpFetching;
    }

    public void setUpFetching(boolean upFetching) {
        this.mUpFetching = upFetching;
    }

    public void setUpFetchListener(UpFetchListener upFetchListener) {
        mUpFetchListener = upFetchListener;
    }

    public interface UpFetchListener {
        void onUpFetch();
    }

    /**
     * up fetch end
     */
    public void setNotDoAnimationCount(int count) {
        mLastPosition = count;
    }

    public void setOnLoadDataListener(OnLoadDataListener<T> onLoadDataListener) {
        mOnLoadDataListener = onLoadDataListener;
    }

    public void setLoadDataCountPerRequest(int loadDataCountPerRequest) {
        mLoadDataCountPerRequest = loadDataCountPerRequest;
    }

    /**
     * Set custom load more
     *
     * @param loadingView 加载视图
     */
    public void setLoadMoreView(LoadMoreView loadingView) {
        this.mLoadMoreView = loadingView;
    }

    /**
     * Load more view count
     *
     * @return 0 or 1
     */
    public int getLoadMoreViewCount() {
        if (!mLoadMoreEnable) {
            return 0;
        }
        if (mLoadMoreView.isLoadEndMoreGone()) {
            return 0;
        }
        if (mData.isEmpty()) {
            return 0;
        }
        return 1;
    }

    /**
     * Gets to load more locations
     *
     * @return
     */
    public int getLoadMoreViewPosition() {
        return getHeaderLayoutCount() + mData.size() + getFooterLayoutCount();
    }


    /**
     * Refresh end, no more data
     *
     * @param gone if true gone the load more view
     */
    private void loadMoreEnd(boolean gone) {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoadMoreEnable = false;
        mLoadMoreView.setLoadMoreEndGone(gone);
        notifyItemRemoved(getLoadMoreViewPosition());
        if (gone) {
        } else {
            mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_END);
            notifyItemChanged(getLoadMoreViewPosition());
        }
    }


    private int parseNewDataRealSize(List<T> newData) {
        if (newData == null) {
            return 0;
        }
        int count = 0;
        for (T t : newData) {
            if (t.isValidData()) {
                count++;
            }
        }
        return count;
    }


    private OnLoadDataCompleteCallback<T> mOnLoadMoreCompleteCallback = new OnLoadDataCompleteCallback<T>() {
        @Override
        public void onLoadDataSuccess(final List<T> newData) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    addData(newData);
                    resetLoadMoreView();
                    if (parseNewDataRealSize(newData) < mLoadDataCountPerRequest) {
                        loadMoreEnd(true);
                    }
                }
            });
        }

        @Override
        public void onLoadDataFail() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    resetLoadMoreView();
                    mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_FAIL);
                }
            });
        }

        @Override
        public void onLoadDataFail(final int extraStatus) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    resetLoadMoreView();
                    mLoadMoreView.setLoadMoreStatus(extraStatus);
                }
            });
        }

        private void resetLoadMoreView() {
            if (getLoadMoreViewCount() != 0) {
                mLoading = false;
                mLoadMoreEnable = true;
                if (mResetManuallyLoadMoreEnable) {
                    mManuallyLoadMoreEnable = true;
                }
                mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                notifyItemChanged(getLoadMoreViewPosition());
            }
        }
    };

    private OnLoadDataCompleteCallback<T> mOnRefreshCompleteCallback = new OnLoadDataCompleteCallback<T>() {
        @Override
        public void onLoadDataSuccess(final List<T> newData) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mRefresh.setRefreshing(false);
                    mLoading = false;
                    setNewData(newData);
                }
            });
        }

        @Override
        public void onLoadDataFail() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mRefresh.setRefreshing(false);
                    mLoading = false;
                    if (mData.size() <= 0) {
                        mStatusView.showStatus(StatusView.STATUS_NO_NET);
                        setEnableRefreshInternal(false);
                    } else {
//                        Toast.makeText(mContext, mNoNetText, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onLoadDataFail(final int extraStatus) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mRefresh.setRefreshing(false);
                    mLoading = false;
                    if (mData.size() <= 0) {
                        mStatusView.showStatus(extraStatus);
                        setEnableRefreshInternal(false);
                    } else {
//                        Toast.makeText(mContext, mNoNetText, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    /**
     * 当NestedScrollView嵌套RecyclerView，在RecyclerView实例化的时候
     * 会导致RecyclerView中所有的item（不管item是否可见）都会被调用onBindViewHolder()。
     * 此时BaseQuickAdapter会在拉取15条数据之后调用autoLoadMore()，继续拉取15条，
     * 导致RecyclerView实例化结束之后就把所有的数据都拉取出来，不再是用户上滑到第15条数据时再拉取数据。
     * <p>
     * 调用setEnableManualLoadMore方法，会屏蔽autoLoadMore()方法，只有当NestedScrollView滑到最低端时，
     * 调用manuallyLoadMore()解除屏蔽，才会继续拉取15条数据，实现用户上滑到第15条数据时再拉取数据的效果。
     * <p>
     * 当mLoadMoreEnable为true时使用
     *
     * @param enable
     */
    public void setEnableManualLoadMore(boolean enable) {
        mManuallyLoadMoreEnable = enable;
        mResetManuallyLoadMoreEnable = enable;
    }

    public void manuallyLoadMore() {
        mManuallyLoadMoreEnable = false;
        if (mViewType != STATE_VIEW) {
            notifyItemChanged(mData.size());
        }
    }

    public boolean isLoading() {
        return mLoading;
    }

    /**
     * Set the enabled state of load more.
     *
     * @param enable True if load more is enabled, false otherwise.
     */
    public void setEnableLoadMore(boolean enable) {
        mUserDefineEnableLoadMore = enable;
        setEnableLoadMoreInternal(enable);

    }

    private void setEnableLoadMoreInternal(boolean enable) {

        if (enable != mLoadMoreEnable) {
            if (enable) {
                mLoadMoreView.setLoadMoreEndGone(false);
                mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                notifyItemInserted(getLoadMoreViewPosition());
            } else {
                mLoadMoreView.setLoadMoreEndGone(true);
                notifyItemRemoved(getLoadMoreViewPosition());
            }
        }
        mLoadMoreEnable = enable;
    }

    /****************下拉刷新控件行为**************************/
    public void setEnableRefresh(boolean enable) {
        mUserDefineEnableRefresh = enable;
        setEnableRefreshInternal(enable);
    }

    private void setEnableRefreshInternal(boolean enable) {
        dismissRefresh();
        mRefresh.setEnable(enable);
    }

    //隐藏刷新状态
    private void dismissRefresh() {
        if (mRefresh != null && mRefresh.isRefreshing()) {
            mRefresh.setRefreshing(false);
        }
    }

    public void setColorSchemeResources(@ColorRes int... colorResIds) {
        mRefresh.setColorSchemeResources(colorResIds);
    }

    /**
     * Sets the duration of the animation.
     *
     * @param duration The length of the animation, in milliseconds.
     */
    public void setDuration(int duration) {
        mDuration = duration;
    }

    public BaseQuickAdapter() {
        mData = new BaseCollection<>();
        mRefresh = getIPullToRefresh();
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 子类重写RecyclerViewWrapper，可以动态选择SwipeRefreshLayout嵌套在RecyclerView的某个父布局中
     *
     * @return
     */
    public IPullToRefresh getIPullToRefresh() {
        return new RecyclerViewWrapper();
    }

    /**
     * setting up a new instance to data;
     *
     * @param data
     */
    public void setNewData(@Nullable List<T> data) {
        this.mData.setNewData(data);
        mLastPosition = -1;
        notifyDataSetChanged();
    }

    /**
     * add one new data in to certain location
     *
     * @param position
     */
    public void addData(@IntRange(from = 0) int position, @NonNull T data) {
        mData.add(position, data);
        notifyItemInserted(position + getHeaderLayoutCount());
        compatibilityDataSizeChanged(1);
    }

    /**
     * add one new data
     */
    public void addData(@NonNull T data) {
        mData.add(data);
        notifyItemInserted(mData.size() + getHeaderLayoutCount());
        compatibilityDataSizeChanged(1);
    }

    /**
     * remove the item associated with the specified position of adapter
     *
     * @param position
     */
    public void remove(@IntRange(from = 0) int position) {
        if (mData == null
                || position < 0
                || position >= mData.size()) return;

        T entity = mData.get(position);
        if (entity instanceof IExpandable) {
            removeAllChild((IExpandable) entity, position);
        }
        removeDataFromParent(entity);

        mData.remove(position);
        int internalPosition = position + getHeaderLayoutCount();
        notifyItemRemoved(internalPosition);
        compatibilityDataSizeChanged(0);
        notifyItemRangeChanged(internalPosition, mData.size() - internalPosition);
    }

    /**
     * 移除父控件时，若父控件处于展开状态，则先移除其所有的子控件
     *
     * @param parent         父控件实体
     * @param parentPosition 父控件位置
     */
    protected void removeAllChild(IExpandable parent, int parentPosition) {
        if (parent.isExpanded()) {
            List subItems = parent.getSubItems();
            if (subItems == null || subItems.size() == 0) return;

            int childSize = subItems.size();
            for (int i = 0; i < childSize; i++) {
                remove(parentPosition + 1);
            }
        }
    }

    /**
     * 移除子控件时，移除父控件实体类中相关子控件数据，避免关闭后再次展开数据重现
     *
     * @param child 子控件实体
     */
    protected void removeDataFromParent(T child) {
        int position = getParentPosition(child);
        if (position >= 0) {
            IExpandable parent = (IExpandable) mData.get(position);
            parent.getSubItems().remove(child);
        }
    }

    /**
     * change data
     */
    public void setData(@IntRange(from = 0) int index, @NonNull T data) {
        mData.set(index, data);
        notifyItemChanged(index + getHeaderLayoutCount());
    }

    /**
     * add new data in to certain location
     *
     * @param position the insert position
     * @param newData  the new data collection
     */
    public void addData(@IntRange(from = 0) int position, @NonNull Collection<? extends T> newData) {
        mData.addAll(position, newData);
        notifyItemRangeInserted(position + getHeaderLayoutCount(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    /**
     * add new data to the end of mData
     *
     * @param newData the new data collection
     */
    public void addData(@NonNull Collection<? extends T> newData) {
        mData.addAll(newData);
        notifyItemRangeInserted(mData.size() - newData.size() + getHeaderLayoutCount(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    /**
     * use data to replace all item in mData. this method is different {@link #setNewData(List)},
     * it doesn't change the mData reference
     *
     * @param data data collection
     */
    public void replaceData(@NonNull Collection<? extends T> data) {
        // 不是同一个引用才清空列表
        if (data != mData) {
            mData.clear();
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    /**
     * compatible getLoadMoreViewCount and getStatusViewCount may change
     *
     * @param size Need compatible data size
     */
    private void compatibilityDataSizeChanged(int size) {
        final int dataSize = mData == null ? 0 : mData.size();
        if (dataSize == size) {
            notifyDataSetChanged();
        }
    }

    /**
     * Get the data of list
     *
     * @return 列表数据
     */
    @NonNull
    public List<T> getData() {
        return mData.getList();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Nullable
    public T getItem(@IntRange(from = 0) int position) {
        if (position < mData.size())
            return mData.get(position);
        else
            return null;
    }

    /**
     * if addHeaderView will be return 1, if not will be return 0
     */
    public int getHeaderLayoutCount() {
        if (mHeaderLayout == null || mHeaderLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * if addFooterView will be return 1, if not will be return 0
     */
    public int getFooterLayoutCount() {
        if (mFooterLayout == null || mFooterLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * if show empty view will be return 1 or not will be return 0
     *
     * @return
     */
    public int getStatusViewCount() {
        if (mStatusLayout == null || mStatusLayout.getChildCount() == 0) {
            return 0;
        }
        if (mData.size() != 0) {
            return 0;
        }
        return 1;
    }


    @Override
    public int getItemCount() {
        int count;
        if (getStatusViewCount() == 1) {
            count = 1;
            if (mHeadAndStatusEnable && getHeaderLayoutCount() != 0) {
                count++;
            }
            if (mFootAndStatusEnable && getFooterLayoutCount() != 0) {
                count++;
            }
        } else {
            count = getHeaderLayoutCount() + mData.size() + getFooterLayoutCount() + getLoadMoreViewCount();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (getStatusViewCount() == 1) {
            boolean header = mHeadAndStatusEnable && getHeaderLayoutCount() != 0;
            switch (position) {
                case 0:
                    if (header) {
                        return HEADER_VIEW;
                    } else {
                        return STATE_VIEW;
                    }
                case 1:
                    if (header) {
                        return STATE_VIEW;
                    } else {
                        return FOOTER_VIEW;
                    }
                case 2:
                    return FOOTER_VIEW;
                default:
                    return STATE_VIEW;
            }
        }
        int numHeaders = getHeaderLayoutCount();
        if (position < numHeaders) {
            return HEADER_VIEW;
        } else {
            int adjPosition = position - numHeaders;
            int adapterCount = mData.size();
            if (adjPosition < adapterCount) {
                return getDefItemViewType(adjPosition);
            } else {
                adjPosition = adjPosition - adapterCount;
                int numFooters = getFooterLayoutCount();
                if (adjPosition < numFooters) {
                    return FOOTER_VIEW;
                } else {
                    return LOADING_VIEW;
                }
            }
        }
    }

    protected int getDefItemViewType(int position) {
        int viewType = mData.get(position).getItemType();
        if (mData.get(position).isEmpty())
            viewType = EMPTY_VIEW;
        return viewType;
    }

    @Override
    public K onCreateViewHolder(ViewGroup parent, int viewType) {
        K baseViewHolder = null;
        this.mContext = parent.getContext();
        this.mLayoutInflater = LayoutInflater.from(mContext);
        mViewType = viewType;
        switch (viewType) {
            case LOADING_VIEW:
                baseViewHolder = getLoadingView(parent);
                break;
            case HEADER_VIEW:
                baseViewHolder = createBaseViewHolder(mHeaderLayout);
                break;
            case STATE_VIEW:
                baseViewHolder = createBaseViewHolder(mStatusLayout);
                break;
            case FOOTER_VIEW:
                baseViewHolder = createBaseViewHolder(mFooterLayout);
                break;
            case EMPTY_VIEW:
                baseViewHolder = createBaseViewHolder(new View(parent.getContext()));
                break;
            default:
                baseViewHolder = onCreateViewHolderByType(parent, viewType);
                bindViewClickListener(baseViewHolder);
        }
        baseViewHolder.setAdapter(this);
        return baseViewHolder;

    }

    private K getLoadingView(ViewGroup parent) {
        View view = getItemView(mLoadMoreView.getLayoutId(), parent);
        K holder = createBaseViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManuallyLoadMoreEnable = false;
                if (mLoadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_FAIL) {
                    notifyLoadMoreToLoading();
                }
                if (mEnableLoadMoreEndClick && mLoadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_END) {
                    notifyLoadMoreToLoading();
                }
            }
        });
        return holder;
    }

    /**
     * The notification starts the callback and loads more
     */
    public void notifyLoadMoreToLoading() {
        if (mLoadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_LOADING) {
            return;
        }
        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        notifyItemChanged(getLoadMoreViewPosition());
    }

    /**
     * Load more without data when settings are clicked loaded
     *
     * @param enable
     */
    public void enableLoadMoreEndClick(boolean enable) {
        mEnableLoadMoreEndClick = enable;
    }

    /**
     * Called when a view created by this adapter has been attached to a window.
     * simple to solve item will layout using all
     * {@link #setFullSpan(RecyclerView.ViewHolder)}
     *
     * @param holder
     */
    @Override
    public void onViewAttachedToWindow(K holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == STATE_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW) {
            setFullSpan(holder);
        } else {
            addAnimation(holder);
        }
    }

    /**
     * When set to true, the item will layout using all span area. That means, if orientation
     * is vertical, the view will have full width; if orientation is horizontal, the view will
     * have full height.
     * if the hold view use StaggeredGridLayoutManager they should using all span area
     *
     * @param holder True if this item should traverse all spans.
     */
    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder
                    .itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    // true to let recycler view load data automatically
    // false to request
    public void setAutoPreLoad(boolean autoPreLoad) {
        mIsAutoPreLoad = autoPreLoad;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (mStatusView == null) {
            SimpleStatusView simpleStatusView = new SimpleStatusView();
            simpleStatusView.init(recyclerView.getContext());
            setStatusView(simpleStatusView);
        }
        mRecyclerView = recyclerView;
        mRefresh.attachRecyclerView(recyclerView);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mLoading) {
                    refreshData();
                }
            }
        });

        if (mStatusView != null) {
            showLoadingView();
            if (mIsAutoPreLoad)
                refreshData();
        }
        registerAdapterDataObserver(mDataObserver);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            final GridLayoutManager.SpanSizeLookup originalSpanSizeLookup = gridManager.getSpanSizeLookup();
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    if (type == HEADER_VIEW && isHeaderViewAsFlow()) {
                        return 1;
                    }
                    if (type == FOOTER_VIEW && isFooterViewAsFlow()) {
                        return 1;
                    }
                    return isFixedViewType(type) ? gridManager.getSpanCount() : originalSpanSizeLookup.getSpanSize(position);
                }


            });
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        unregisterAdapterDataObserver(mDataObserver);
    }

    protected boolean isFixedViewType(int type) {
        return type == STATE_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type ==
                LOADING_VIEW;
    }

    /**
     * if asFlow is true, footer/header will arrange like normal item view.
     * only works when use {@link GridLayoutManager},and it will ignore span size.
     */
    private boolean headerViewAsFlow, footerViewAsFlow;

    public void setHeaderViewAsFlow(boolean headerViewAsFlow) {
        this.headerViewAsFlow = headerViewAsFlow;
    }

    public boolean isHeaderViewAsFlow() {
        return headerViewAsFlow;
    }

    public void setFooterViewAsFlow(boolean footerViewAsFlow) {
        this.footerViewAsFlow = footerViewAsFlow;
    }

    public boolean isFooterViewAsFlow() {
        return footerViewAsFlow;
    }

    /**
     * To bind different types of holder and solve different the bind events
     *
     * @param holder
     * @param position
     * @see #getDefItemViewType(int)
     */
    @Override
    public void onBindViewHolder(K holder, int position) {
        //Add up fetch logic, almost like load more, but simpler.
        autoUpFetch(position);
        //Do not move position, need to change before LoadMoreView binding
        autoLoadMore(position);
        int viewType = holder.getItemViewType();

        switch (viewType) {
            case 0:
                onBindData(holder, getItem(position - getHeaderLayoutCount()), position - getHeaderLayoutCount());
                break;
            case LOADING_VIEW:
                mLoadMoreView.convert(holder);
                break;
            case HEADER_VIEW:
                break;
            case STATE_VIEW:
                break;
            case FOOTER_VIEW:
                break;
            default:
                onBindData(holder, getItem(position - getHeaderLayoutCount()), position - getHeaderLayoutCount());
                break;
        }
    }

    private void bindViewClickListener(final BaseViewHolder baseViewHolder) {
        if (baseViewHolder == null) {
            return;
        }
        final View view = baseViewHolder.itemView;
        if (view == null) {
            return;
        }
        if (getOnItemClickListener() != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setOnItemClick(v, baseViewHolder.getLayoutPosition() - getHeaderLayoutCount());
                }
            });
        }
        if (getOnItemLongClickListener() != null) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return setOnItemLongClick(v, baseViewHolder.getLayoutPosition() - getHeaderLayoutCount());
                }
            });
        }
    }

    /**
     * override this method if you want to override click event logic
     *
     * @param v
     * @param position
     */
    public void setOnItemClick(View v, int position) {
        getOnItemClickListener().onItemClick(BaseQuickAdapter.this, v, position);
    }

    /**
     * override this method if you want to override longClick event logic
     *
     * @param v
     * @param position
     * @return
     */
    public boolean setOnItemLongClick(View v, int position) {
        return getOnItemLongClickListener().onItemLongClick(BaseQuickAdapter.this, v, position);
    }

    protected abstract K onCreateViewHolderByType(ViewGroup parent, int viewType);

    /**
     * if you want to use subclass of BaseViewHolder in the adapter,
     * you must override the method to create new ViewHolder.
     *
     * @param view view
     * @return new ViewHolder
     */
    @SuppressWarnings("unchecked")
    protected K createBaseViewHolder(View view) {
        Class temp = getClass();
        Class z = null;
        while (z == null && null != temp) {
            z = getInstancedGenericKClass(temp);
            temp = temp.getSuperclass();
        }
        K k;
        // 泛型擦除会导致z为null
        if (z == null) {
            k = (K) new BaseViewHolder(view);
        } else {
            k = createGenericKInstance(z, view);
        }
        return k != null ? k : (K) new BaseViewHolder(view);
    }

    /**
     * try to create Generic K instance
     *
     * @param z
     * @param view
     * @return
     */
    @SuppressWarnings("unchecked")
    private K createGenericKInstance(Class z, View view) {
        try {
            Constructor constructor;
            // inner and unstatic class
            if (z.isMemberClass() && !Modifier.isStatic(z.getModifiers())) {
                constructor = z.getDeclaredConstructor(getClass(), View.class);
                constructor.setAccessible(true);
                return (K) constructor.newInstance(this, view);
            } else {
                constructor = z.getDeclaredConstructor(View.class);
                constructor.setAccessible(true);
                return (K) constructor.newInstance(view);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get generic parameter K
     *
     * @param z
     * @return
     */
    private Class getInstancedGenericKClass(Class z) {
        Type type = z.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type temp : types) {
                if (temp instanceof Class) {
                    Class tempClass = (Class) temp;
                    if (BaseViewHolder.class.isAssignableFrom(tempClass)) {
                        return tempClass;
                    }
                } else if (temp instanceof ParameterizedType) {
                    Type rawType = ((ParameterizedType) temp).getRawType();
                    if (rawType instanceof Class && BaseViewHolder.class.isAssignableFrom((Class<?>) rawType)) {
                        return (Class<?>) rawType;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return root layout of header
     */

    public LinearLayout getHeaderLayout() {
        return mHeaderLayout;
    }

    /**
     * Return root layout of footer
     */
    public LinearLayout getFooterLayout() {
        return mFooterLayout;
    }

    /**
     * Append header to the rear of the mHeaderLayout.
     *
     * @param header
     */
    public int addHeaderView(View header) {
        return addHeaderView(header, -1);
    }

    /**
     * Add header view to mHeaderLayout and set header view position in mHeaderLayout.
     * When index = -1 or index >= child count in mHeaderLayout,
     * the effect of this method is the same as that of {@link #addHeaderView(View)}.
     *
     * @param header
     * @param index  the position in mHeaderLayout of this header.
     *               When index = -1 or index >= child count in mHeaderLayout,
     *               the effect of this method is the same as that of {@link #addHeaderView(View)}.
     */
    public int addHeaderView(View header, int index) {
        return addHeaderView(header, index, LinearLayout.VERTICAL);
    }

    /**
     * @param header
     * @param index
     * @param orientation
     */
    public int addHeaderView(View header, int index, int orientation) {
        if (mHeaderLayout == null) {
            mHeaderLayout = new LinearLayout(header.getContext());
            if (orientation == LinearLayout.VERTICAL) {
                mHeaderLayout.setOrientation(LinearLayout.VERTICAL);
                mHeaderLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mHeaderLayout.setOrientation(LinearLayout.HORIZONTAL);
                mHeaderLayout.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        final int childCount = mHeaderLayout.getChildCount();
        if (index < 0 || index > childCount) {
            index = childCount;
        }
        mHeaderLayout.addView(header, index);
        if (mHeaderLayout.getChildCount() == 1) {
            int position = getHeaderViewPosition();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }
        return index;
    }

    public int setHeaderView(View header) {
        return setHeaderView(header, 0, LinearLayout.VERTICAL);
    }

    public int setHeaderView(View header, int index) {
        return setHeaderView(header, index, LinearLayout.VERTICAL);
    }

    public int setHeaderView(View header, int index, int orientation) {
        if (mHeaderLayout == null || mHeaderLayout.getChildCount() <= index) {
            return addHeaderView(header, index, orientation);
        } else {
            mHeaderLayout.removeViewAt(index);
            mHeaderLayout.addView(header, index);
            return index;
        }
    }

    /**
     * Append footer to the rear of the mFooterLayout.
     *
     * @param footer
     */
    public int addFooterView(View footer) {
        return addFooterView(footer, -1, LinearLayout.VERTICAL);
    }

    public int addFooterView(View footer, int index) {
        return addFooterView(footer, index, LinearLayout.VERTICAL);
    }

    /**
     * Add footer view to mFooterLayout and set footer view position in mFooterLayout.
     * When index = -1 or index >= child count in mFooterLayout,
     * the effect of this method is the same as that of {@link #addFooterView(View)}.
     *
     * @param footer
     * @param index  the position in mFooterLayout of this footer.
     *               When index = -1 or index >= child count in mFooterLayout,
     *               the effect of this method is the same as that of {@link #addFooterView(View)}.
     */
    public int addFooterView(View footer, int index, int orientation) {
        if (mFooterLayout == null) {
            mFooterLayout = new LinearLayout(footer.getContext());
            if (orientation == LinearLayout.VERTICAL) {
                mFooterLayout.setOrientation(LinearLayout.VERTICAL);
                mFooterLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mFooterLayout.setOrientation(LinearLayout.HORIZONTAL);
                mFooterLayout.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        final int childCount = mFooterLayout.getChildCount();
        if (index < 0 || index > childCount) {
            index = childCount;
        }
        mFooterLayout.addView(footer, index);
        if (mFooterLayout.getChildCount() == 1) {
            int position = getFooterViewPosition();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }
        return index;
    }

    public int setFooterView(View header) {
        return setFooterView(header, 0, LinearLayout.VERTICAL);
    }

    public int setFooterView(View header, int index) {
        return setFooterView(header, index, LinearLayout.VERTICAL);
    }

    public int setFooterView(View header, int index, int orientation) {
        if (mFooterLayout == null || mFooterLayout.getChildCount() <= index) {
            return addFooterView(header, index, orientation);
        } else {
            mFooterLayout.removeViewAt(index);
            mFooterLayout.addView(header, index);
            return index;
        }
    }

    /**
     * remove header view from mHeaderLayout.
     * When the child count of mHeaderLayout is 0, mHeaderLayout will be set to null.
     *
     * @param header
     */
    public void removeHeaderView(View header) {
        if (getHeaderLayoutCount() == 0) return;

        mHeaderLayout.removeView(header);
        if (mHeaderLayout.getChildCount() == 0) {
            int position = getHeaderViewPosition();
            if (position != -1) {
                notifyItemRemoved(position);
            }
        }
    }

    /**
     * remove footer view from mFooterLayout,
     * When the child count of mFooterLayout is 0, mFooterLayout will be set to null.
     *
     * @param footer
     */
    public void removeFooterView(View footer) {
        if (getFooterLayoutCount() == 0) return;

        mFooterLayout.removeView(footer);
        if (mFooterLayout.getChildCount() == 0) {
            int position = getFooterViewPosition();
            if (position != -1) {
                notifyItemRemoved(position);
            }
        }
    }

    /**
     * remove all header view from mHeaderLayout and set null to mHeaderLayout
     */
    public void removeAllHeaderView() {
        if (getHeaderLayoutCount() == 0) return;

        mHeaderLayout.removeAllViews();
        int position = getHeaderViewPosition();
        if (position != -1) {
            notifyItemRemoved(position);
        }
    }

    /**
     * remove all footer view from mFooterLayout and set null to mFooterLayout
     */
    public void removeAllFooterView() {
        if (getFooterLayoutCount() == 0) return;

        mFooterLayout.removeAllViews();
        int position = getFooterViewPosition();
        if (position != -1) {
            notifyItemRemoved(position);
        }
    }

    private int getHeaderViewPosition() {
        //Return to header view notify position
        if (getStatusViewCount() == 1) {
            if (mHeadAndStatusEnable) {
                return 0;
            }
        } else {
            return 0;
        }
        return -1;
    }

    private int getFooterViewPosition() {
        //Return to footer view notify position
        if (getStatusViewCount() == 1) {
            int position = 1;
            if (mHeadAndStatusEnable && getHeaderLayoutCount() != 0) {
                position++;
            }
            if (mFootAndStatusEnable) {
                return position;
            }
        } else {
            return getHeaderLayoutCount() + mData.size();
        }
        return -1;
    }


    /************************当RecyclerView无数据时加载状态信息*********************************/
    public void setStatusView(StatusView statusView) {
        initStatusView(statusView);
        setStatusView(statusView.getRoot());
    }

    /**
     * 初始化Stataus
     */
    private void initStatusView(StatusView statusView) {
        mStatusView = statusView;
        mStatusView.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = mStatusView.getStatus();
                if (status == StatusView.STATUS_NO_NET || status == StatusView.STATUS_LOAD_FAILED) {
                    showLoadingView();
                    refreshData();
                }
            }
        });
    }

    public void showStatus(int status) {
        if (!mLoading) {
            setEnableRefreshInternal(false);
            mStatusView.showStatus(status);
        }
    }

    /**
     * 用于重新刷新数据，在刷新过程中不会清除原有的数据
     */
    public void showLoadingAndRefreshData() {
        showLoadingView();
        refreshData();
    }

    public void showLoadingAndRefreshData(long delay) {
        showLoadingView();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        }, delay);
    }

    public void manuallyRefreshData() {
        refreshData();
    }

    protected void refreshData() {
        if (!mLoading) {
            mLoading = true;
            mOnLoadDataListener.onRequestData(mData.getHeadIndex(), mLoadDataCountPerRequest, mOnRefreshCompleteCallback);
        }
    }

    /**
     * loading状态
     */
    protected void showLoadingView() {
        if (mData.size() <= 0) {
            mStatusView.showStatus(StatusView.STATUS_LOADING);
            setEnableRefreshInternal(false);
        }
    }

    /*********************************************************/


    private void setStatusView(View statusView) {
        boolean insert = false;
        if (mStatusLayout == null) {
            mStatusLayout = new FrameLayout(statusView.getContext());
            final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            final ViewGroup.LayoutParams lp = statusView.getLayoutParams();
            if (lp != null) {
                layoutParams.width = lp.width;
                layoutParams.height = lp.height;
            }
            mStatusLayout.setLayoutParams(layoutParams);
            insert = true;
        }
        mStatusLayout.removeAllViews();
        mStatusLayout.addView(statusView);
        if (insert) {
            if (getStatusViewCount() == 1) {
                int position = 0;
                if (mHeadAndStatusEnable && getHeaderLayoutCount() != 0) {
                    position++;
                }
                notifyItemInserted(position);
            }
        }
    }


    /**
     * Call before {@link RecyclerView#setAdapter(RecyclerView.Adapter)}
     *
     * @param isHeadAndStatus false will not show headView if the data is empty true will show emptyView and headView
     */
    public void setHeaderAndStatus(boolean isHeadAndStatus) {
        setHeaderFooterStatus(isHeadAndStatus, false);
    }

    /**
     * set emptyView show if adapter is empty and want to show headview and footview
     * Call before {@link RecyclerView#setAdapter(RecyclerView.Adapter)}
     *
     * @param isHeadAndStatus
     * @param isFootAndStatus
     */
    public void setHeaderFooterStatus(boolean isHeadAndStatus, boolean isFootAndStatus) {
        mHeadAndStatusEnable = isHeadAndStatus;
        mFootAndStatusEnable = isFootAndStatus;
    }

    /**
     * When the current adapter is empty, the BaseQuickAdapter can display a special view
     * called the empty view. The empty view is used to provide feedback to the user
     * that no data is available in this AdapterView.
     *
     * @return The view to show if the adapter is empty.
     */
    public View getStatusView() {
        return mStatusLayout;
    }


    private int mPreLoadNumber = 1;

    public void setPreLoadNumber(int preLoadNumber) {
        if (preLoadNumber > 1) {
            mPreLoadNumber = preLoadNumber;
        }
    }

    private void autoLoadMore(int position) {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        if (position < getItemCount() - mPreLoadNumber) {
            return;
        }
        if (mLoadMoreView.getLoadMoreStatus() != LoadMoreView.STATUS_DEFAULT) {
            return;
        }
        if (mManuallyLoadMoreEnable) {
            return;
        }
        if (!mLoading && mLoadMoreEnable) {
            mLoading = true;
            mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_LOADING);
            if (getRecyclerView() != null) {
                getRecyclerView().post(new Runnable() {
                    @Override
                    public void run() {
                        mOnLoadDataListener.onRequestData(mData.getTailIndex(), mLoadDataCountPerRequest, mOnLoadMoreCompleteCallback);
                    }
                });
            } else {
                mOnLoadDataListener.onRequestData(mData.getTailIndex(), mLoadDataCountPerRequest, mOnLoadMoreCompleteCallback);
            }
        }
    }

    /**
     * add animation when you want to show time
     *
     * @param holder
     */
    private void addAnimation(RecyclerView.ViewHolder holder) {
        if (mOpenAnimationEnable) {
            if (!mFirstOnlyEnable || holder.getLayoutPosition() > mLastPosition) {
                BaseAnimation animation = null;
                if (mCustomAnimation != null) {
                    animation = mCustomAnimation;
                } else {
                    animation = mSelectAnimation;
                }
                for (Animator anim : animation.getAnimators(holder.itemView)) {
                    startAnim(anim, holder.getLayoutPosition());
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

    /**
     * set anim to start when loading
     *
     * @param anim
     * @param index
     */
    protected void startAnim(Animator anim, int index) {
        anim.setDuration(mDuration).start();
        anim.setInterpolator(mInterpolator);
    }

    /**
     * @param layoutResId ID for an XML layout resource to load
     * @param parent      Optional view to be the parent of the generated hierarchy or else simply an object that
     *                    provides a set of LayoutParams values for root of the returned
     *                    hierarchy
     * @return view will be return
     */
    public View getItemView(@LayoutRes int layoutResId, ViewGroup parent) {
        return mLayoutInflater.inflate(layoutResId, parent, false);
    }

    public interface RequestLoadMoreListener {

        void onLoadMoreRequested();

    }


    /**
     * Set the view animation type.
     *
     * @param animationType One of {@link #ALPHA_IN}, {@link #SCALE_IN}, {@link #SLIDE_IN_BOTTOM},
     *                      {@link #SLIDE_IN_LEFT}, {@link #SLIDE_IN_RIGHT}.
     */
    public void openLoadAnimation(@AnimationType int animationType) {
        this.mOpenAnimationEnable = true;
        mCustomAnimation = null;
        switch (animationType) {
            case ALPHA_IN:
                mSelectAnimation = new AlphaInAnimation();
                break;
            case SCALE_IN:
                mSelectAnimation = new ScaleInAnimation();
                break;
            case SLIDE_IN_BOTTOM:
                mSelectAnimation = new SlideInBottomAnimation();
                break;
            case SLIDE_IN_LEFT:
                mSelectAnimation = new SlideInLeftAnimation();
                break;
            case SLIDE_IN_RIGHT:
                mSelectAnimation = new SlideInRightAnimation();
                break;
            default:
                break;
        }
    }

    /**
     * Set Custom ObjectAnimator
     *
     * @param animation ObjectAnimator
     */
    public void openLoadAnimation(BaseAnimation animation) {
        this.mOpenAnimationEnable = true;
        this.mCustomAnimation = animation;
    }

    /**
     * To open the animation when loading
     */
    public void openLoadAnimation() {
        this.mOpenAnimationEnable = true;
    }

    /**
     * {@link #addAnimation(RecyclerView.ViewHolder)}
     *
     * @param firstOnly true just show anim when first loading false show anim when load the data every time
     */
    public void isFirstOnly(boolean firstOnly) {
        this.mFirstOnlyEnable = firstOnly;
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param viewHolder A fully initialized helper.
     * @param item       The item that needs to be displayed.
     */
    protected void onBindData(K viewHolder, T item, int dataPosition) {
        viewHolder.setData(item, dataPosition);
    }

    /**
     * get the specific view by position,e.g. getViewByPosition(2, R.id.textView)
     */
    @Nullable
    public View getViewByPosition(int position, @IdRes int viewId) {
        checkNotNull();
        return getViewByPosition(getRecyclerView(), position, viewId);
    }

    @Nullable
    public View getViewByPosition(RecyclerView recyclerView, int position, @IdRes int viewId) {
        if (recyclerView == null) {
            return null;
        }
        BaseViewHolder viewHolder = (BaseViewHolder) recyclerView.findViewHolderForLayoutPosition(position);
        if (viewHolder == null) {
            return null;
        }
        return viewHolder.getView(viewId);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("unchecked")
    private int recursiveExpand(int position, @NonNull List list) {
        int count = list.size();
        int pos = position + list.size() - 1;
        for (int i = list.size() - 1; i >= 0; i--, pos--) {
            if (list.get(i) instanceof IExpandable) {
                IExpandable item = (IExpandable) list.get(i);
                if (item.isExpanded() && hasSubItems(item)) {
                    List subList = item.getSubItems();
                    mData.addAll(pos + 1, subList);
                    int subItemCount = recursiveExpand(pos + 1, subList);
                    count += subItemCount;
                }
            }
        }
        return count;

    }

    /**
     * Expand an expandable item
     *
     * @param position     position of the item
     * @param animate      expand items with animation
     * @param shouldNotify notify the RecyclerView to rebind items, <strong>false</strong> if you want to do it
     *                     yourself.
     * @return the number of items that have been added.
     * 目前只兼容有动画的，没有动画的有BUG，建议animate为true
     */
    @SuppressWarnings("unchecked")
    public int expand(@IntRange(from = 0) int position, boolean animate, boolean shouldNotify) {
        position -= getHeaderLayoutCount();

        IExpandable expandable = getExpandableItem(position);
        if (expandable == null) {
            return 0;
        }
        if (!hasSubItems(expandable)) {
            expandable.setExpanded(true);
            notifyItemChanged(position);
            return 0;
        }
        int subItemCount = 0;
        if (!expandable.isExpanded()) {
            List list = expandable.getSubItems();
            mData.addAll(position + 1, list);
            subItemCount += recursiveExpand(position + 1, list);

            expandable.setExpanded(true);
//            subItemCount += list.size();
        }
        int parentPos = position + getHeaderLayoutCount();
        if (shouldNotify) {
            if (animate) {
                notifyItemChanged(parentPos);
                notifyItemRangeInserted(parentPos + 1, subItemCount);
            } else {
                notifyDataSetChanged();
            }
        }
        return subItemCount;
    }

    /**
     * Expand an expandable item
     *
     * @param position position of the item, which includes the header layout count.
     * @param animate  expand items with animation
     * @return the number of items that have been added.
     */
    public int expand(@IntRange(from = 0) int position, boolean animate) {
        return expand(position, animate, true);
    }

    /**
     * Expand an expandable item with animation.
     *
     * @param position position of the item, which includes the header layout count.
     * @return the number of items that have been added.
     */
    public int expand(@IntRange(from = 0) int position) {
        return expand(position, true, true);
    }

    public int expandAll(int position, boolean animate, boolean notify) {
        position -= getHeaderLayoutCount();

        T endItem = null;
        if (position + 1 < this.mData.size()) {
            endItem = getItem(position + 1);
        }

        IExpandable expandable = getExpandableItem(position);
        if (expandable == null) {
            return 0;
        }

        if (!hasSubItems(expandable)) {
            expandable.setExpanded(true);
            notifyItemChanged(position);
            return 0;
        }

        int count = expand(position + getHeaderLayoutCount(), false, false);
        for (int i = position + 1; i < this.mData.size(); i++) {
            T item = getItem(i);

            if (item == endItem) {
                break;
            }
            if (isExpandable(item)) {
                count += expand(i + getHeaderLayoutCount(), false, false);
            }
        }

        if (notify) {
            if (animate) {
                notifyItemRangeInserted(position + getHeaderLayoutCount() + 1, count);
            } else {
                notifyDataSetChanged();
            }
        }
        return count;
    }

    /**
     * expand the item and all its subItems
     *
     * @param position position of the item, which includes the header layout count.
     * @param init     whether you are initializing the recyclerView or not.
     *                 if <strong>true</strong>, it won't notify recyclerView to redraw UI.
     * @return the number of items that have been added to the adapter.
     */
    public int expandAll(int position, boolean init) {
        return expandAll(position, true, !init);
    }

    public void expandAll() {

        for (int i = mData.size() - 1 + getHeaderLayoutCount(); i >= getHeaderLayoutCount(); i--) {
            expandAll(i, false, false);
        }
    }

    @SuppressWarnings("unchecked")
    private int recursiveCollapse(@IntRange(from = 0) int position) {
        T item = getItem(position);
        if (!isExpandable(item)) {
            return 0;
        }
        IExpandable expandable = (IExpandable) item;
        int subItemCount = 0;
        if (expandable.isExpanded()) {
            List<T> subItems = expandable.getSubItems();
            if (null == subItems) return 0;

            for (int i = subItems.size() - 1; i >= 0; i--) {
                T subItem = subItems.get(i);
                int pos = getItemPosition(subItem);
                if (pos < 0) {
                    continue;
                }
                if (subItem instanceof IExpandable) {
                    subItemCount += recursiveCollapse(pos);
                }
                mData.remove(pos);
                subItemCount++;
            }
        }
        return subItemCount;
    }

    /**
     * Collapse an expandable item that has been expanded..
     *
     * @param position the position of the item, which includes the header layout count.
     * @param animate  collapse with animation or not.
     * @param notify   notify the recyclerView refresh UI or not.
     * @return the number of subItems collapsed.
     * 目前只兼容有动画的，没有动画的有BUG，建议animate为true
     */
    public int collapse(@IntRange(from = 0) int position, boolean animate, boolean notify) {
        position -= getHeaderLayoutCount();

        IExpandable expandable = getExpandableItem(position);
        if (expandable == null) {
            return 0;
        }
        int subItemCount = recursiveCollapse(position);
        expandable.setExpanded(false);
        int parentPos = position + getHeaderLayoutCount();
        if (notify) {
            if (animate) {
                notifyItemChanged(parentPos);
                notifyItemRangeRemoved(parentPos + 1, subItemCount);
            } else {
                notifyDataSetChanged();
            }
        }
        return subItemCount;
    }

    /**
     * Collapse an expandable item that has been expanded..
     *
     * @param position the position of the item, which includes the header layout count.
     * @return the number of subItems collapsed.
     */
    public int collapse(@IntRange(from = 0) int position) {
        return collapse(position, true, true);
    }

    /**
     * Collapse an expandable item that has been expanded..
     *
     * @param position the position of the item, which includes the header layout count.
     * @return the number of subItems collapsed.
     */
    public int collapse(@IntRange(from = 0) int position, boolean animate) {
        return collapse(position, animate, true);
    }

    private int getItemPosition(T item) {
        return item != null && !mData.isEmpty() ? mData.indexOf(item) : -1;
    }

    private boolean hasSubItems(IExpandable item) {
        if (item == null) {
            return false;
        }
        List list = item.getSubItems();
        return list != null && list.size() > 0;
    }

    public boolean isExpandable(T item) {
        return item != null && item instanceof IExpandable;
    }

    private IExpandable getExpandableItem(int position) {
        T item = getItem(position);
        if (isExpandable(item)) {
            return (IExpandable) item;
        } else {
            return null;
        }
    }

    /**
     * Get the parent item position of the IExpandable item
     *
     * @return return the closest parent item position of the IExpandable.
     * if the IExpandable item's level is 0, return itself position.
     * if the item's level is negative which mean do not implement this, return a negative
     * if the item is not exist in the data list, return a negative.
     */
    public int getParentPosition(@NonNull T item) {
        int position = getItemPosition(item);
        if (position == -1) {
            return -1;
        }

        // if the item is IExpandable, return a closest IExpandable item position whose level smaller than this.
        // if it is not, return the closest IExpandable item position whose level is not negative
        int level;
        if (item instanceof IExpandable) {
            level = ((IExpandable) item).getLevel();
        } else {
            level = Integer.MAX_VALUE;
        }
        if (level == 0) {
            return position;
        } else if (level == -1) {
            return -1;
        }

        for (int i = position; i >= 0; i--) {
            T temp = mData.get(i);
            if (temp instanceof IExpandable) {
                IExpandable expandable = (IExpandable) temp;
                if (expandable.getLevel() >= 0 && expandable.getLevel() < level) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Interface definition for a callback to be invoked when an itemchild in this
     * view has been clicked
     */
    public interface OnItemChildClickListener {
        /**
         * callback method to be invoked when an item in this view has been
         * click and held
         *
         * @param view     The view whihin the ItemView that was clicked
         * @param position The position of the view int the adapter
         */
        void onItemChildClick(BaseQuickAdapter adapter, View view, int position);
    }


    /**
     * Interface definition for a callback to be invoked when an childView in this
     * view has been clicked and held.
     */
    public interface OnItemChildLongClickListener {
        /**
         * callback method to be invoked when an item in this view has been
         * click and held
         *
         * @param view     The childView whihin the itemView that was clicked and held.
         * @param position The position of the view int the adapter
         * @return true if the callback consumed the long click ,false otherwise
         */
        boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position);
    }

    /**
     * Interface definition for a callback to be invoked when an item in this
     * view has been clicked and held.
     */
    public interface OnItemLongClickListener {
        /**
         * callback method to be invoked when an item in this view has been
         * click and held
         *
         * @param adapter  the adpater
         * @param view     The view whihin the RecyclerView that was clicked and held.
         * @param position The position of the view int the adapter
         * @return true if the callback consumed the long click ,false otherwise
         */
        boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position);
    }


    /**
     * Interface definition for a callback to be invoked when an item in this
     * RecyclerView itemView has been clicked.
     */
    public interface OnItemClickListener {

        /**
         * Callback method to be invoked when an item in this RecyclerView has
         * been clicked.
         *
         * @param adapter  the adpater
         * @param view     The itemView within the RecyclerView that was clicked (this
         *                 will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         */
        void onItemClick(BaseQuickAdapter adapter, View view, int position);
    }

    /**
     * Register a callback to be invoked when an item in this RecyclerView has
     * been clicked.
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * Register a callback to be invoked when an itemchild in View has
     * been  clicked
     *
     * @param listener The callback that will run
     */
    public void setOnItemChildClickListener(OnItemChildClickListener listener) {
        mOnItemChildClickListener = listener;
    }

    /**
     * Register a callback to be invoked when an item in this RecyclerView has
     * been long clicked and held
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    /**
     * Register a callback to be invoked when an itemchild  in this View has
     * been long clicked and held
     *
     * @param listener The callback that will run
     */
    public void setOnItemChildLongClickListener(OnItemChildLongClickListener listener) {
        mOnItemChildLongClickListener = listener;
    }


    /**
     * @return The callback to be invoked with an item in this RecyclerView has
     * been long clicked and held, or null id no callback as been set.
     */
    public final OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }

    /**
     * @return The callback to be invoked with an item in this RecyclerView has
     * been clicked and held, or null id no callback as been set.
     */
    public final OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    /**
     * @return The callback to be invoked with an itemchild in this RecyclerView has
     * been clicked, or null id no callback has been set.
     */
    @Nullable
    public final OnItemChildClickListener getOnItemChildClickListener() {
        return mOnItemChildClickListener;
    }

    /**
     * @return The callback to be invoked with an itemChild in this RecyclerView has
     * been long clicked, or null id no callback has been set.
     */
    @Nullable
    public final OnItemChildLongClickListener getOnItemChildLongClickListener() {
        return mOnItemChildLongClickListener;
    }
}
