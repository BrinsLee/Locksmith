package com.chad.library.adapter.base.status;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import androidx.annotation.StringRes;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chad.library.R;
import com.chad.library.adapter.base.status.setting.StatusBindButtonBackgroundSetting;
import com.chad.library.adapter.base.status.setting.StatusViewSetting;

/**
 * Created by mushroom0417 on 19/12/2017.
 */

public class CommonStatusView extends StatusView {

    private OnStateViewClickObserver mOnStateViewClickObserver;
    private Context mContext;

    public CommonStatusView() {
        setLayoutId(R.layout.view_status);
    }

    @Override
    public int getLayoutId() {
        return mLayoutResourceId;
    }

    @Override
    public CommonStatusView init(Context context) {
        super.init(context);
        mContext = context;
        return this;
    }

    @Override
    protected void onBindState() {
        bindState(STATUS_LOAD_FAILED, R.id.layout_load_failed);
        bindState(STATUS_LOADING, R.id.layout_loading);
        bindState(STATUS_NO_DATA, R.id.layout_no_data);
        bindState(STATUS_NO_NET, R.id.layout_no_net);
        bindState(STATUS_NO_LOGIN, R.id.layout_no_login);
        initStatusLoading();
        initStatusNoData();
        initStatusNoLogin();
        initStatusNoNet();
    }

    protected void initStatusNoNet() {
        if (StatusViewSetting.getInstance().getNoNetStatus() == null
                || StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo() == null) {
            return;
        }
//        ImageView iconNoNet = mViewMap.get(STATUS_NO_NET).findViewById(R.id.image_no_net);
//        if (StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo().getStatusRes() > 0) {
//            iconNoNet.setImageResource(StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo().getStatusRes());
//        }
//        LinearLayout.LayoutParams mParams = (LinearLayout.LayoutParams) iconNoNet.getLayoutParams();
//        if (StatusViewSetting.getInstance().getIconWidth() > 0) {
//            mParams.width = StatusViewSetting.getInstance().getIconWidth();
//        }
//        if (StatusViewSetting.getInstance().getIconHeight() > 0) {
//            mParams.height = StatusViewSetting.getInstance().getIconHeight();
//        }
//        iconNoNet.setLayoutParams(mParams);

        TextView noNetText = mViewMap.get(STATUS_NO_NET).findViewById(R.id.text_no_net);
        if (!TextUtils.isEmpty(StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo().getStatusText())) {
            noNetText.setText(StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo().getStatusText());
        }
        if (StatusViewSetting.getInstance().getTextSize() > 0) {
            noNetText.setTextSize(TypedValue.COMPLEX_UNIT_PX, StatusViewSetting.getInstance().getTextSize());
        }
        noNetText.setTextColor(StatusViewSetting.getInstance().getTextColor());

        if (!TextUtils.isEmpty(StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo().getBtnText())) {
            TextView btnText = mViewMap.get(STATUS_NO_NET).findViewById(R.id.button_refresh);
            if (!TextUtils.isEmpty(StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo().getBtnText())) {
                btnText.setText(StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo().getBtnText());
            }
            if (StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo().getBtnTextSize() > 0) {
                btnText.setTextSize(TypedValue.COMPLEX_UNIT_PX, StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo().getBtnTextSize());
            }
            btnText.setTextColor(StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo().getBtnTextColor());
            bindButtonBackground(btnText);
            btnText.setVisibility(StatusViewSetting.getInstance().getNoNetStatus().getNoNetStatusInfo().getBtnVisibility());
        }
    }

    protected void initStatusNoLogin() {
        if (StatusViewSetting.getInstance().getNoLoginStatus() == null
                || StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo() == null) {
            return;
        }
//        ImageView iconNoLogin = mViewMap.get(STATUS_NO_LOGIN).findViewById(R.id.image_no_login);
//        LinearLayout.LayoutParams mParams = (LinearLayout.LayoutParams) iconNoLogin.getLayoutParams();
//        if (StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getStatusRes() > 0) {
//            iconNoLogin.setImageResource(StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getStatusRes());
//        }
//        if (StatusViewSetting.getInstance().getIconWidth() > 0) {
//            mParams.width = StatusViewSetting.getInstance().getIconWidth();
//        }
//        if (StatusViewSetting.getInstance().getIconHeight() > 0) {
//            mParams.height = StatusViewSetting.getInstance().getIconHeight();
//        }
//        iconNoLogin.setLayoutParams(mParams);

//        TextView noLoginTextView = mViewMap.get(STATUS_NO_LOGIN).findViewById(R.id.text_no_login);
//        if (!TextUtils.isEmpty(StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getStatusText())) {
//            noLoginTextView.setText(StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getStatusText());
//        }
//        if (StatusViewSetting.getInstance().getTextSize() > 0) {
//            noLoginTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, StatusViewSetting.getInstance().getTextSize());
//        }
//        noLoginTextView.setTextColor(StatusViewSetting.getInstance().getTextColor());

        if (!TextUtils.isEmpty(StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getBtnText())) {
            TextView btnText = mViewMap.get(STATUS_NO_LOGIN).findViewById(R.id.button_go_to_login);
            btnText.setText(StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getBtnText());
            if (StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getBtnTextSize() > 0) {
                btnText.setTextSize(TypedValue.COMPLEX_UNIT_PX, StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getBtnTextSize());
            }
            btnText.setTextColor(StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getBtnTextColor());
            bindButtonBackground(btnText);
            btnText.setVisibility(StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getBtnVisibility());
            mViewMap.get(STATUS_NO_LOGIN).findViewById(R.id.button_go_to_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getOnBtnClickListener()) {
                        StatusViewSetting.getInstance().getNoLoginStatus().getNoLoginStatusInfo().getOnBtnClickListener().onClick(v);
                    }
                }
            });
        }
    }

    protected void initStatusNoData() {
        if (StatusViewSetting.getInstance().getNoDataStatus() == null
                || StatusViewSetting.getInstance().getNoDataStatus().getNoDataStatusInfo() == null) {
            return;
        }
        ImageView iconNoData = mViewMap.get(STATUS_NO_DATA).findViewById(R.id.image_no_data);
        if (StatusViewSetting.getInstance().getNoDataStatus().getNoDataStatusInfo().getStatusRes() > 0) {
            iconNoData.setImageResource(StatusViewSetting.getInstance().getNoDataStatus().getNoDataStatusInfo().getStatusRes());
        }
        LinearLayout.LayoutParams mParams = (LinearLayout.LayoutParams) iconNoData.getLayoutParams();
        if (StatusViewSetting.getInstance().getIconWidth() > 0) {
            mParams.width = StatusViewSetting.getInstance().getIconWidth();
        }
        if (StatusViewSetting.getInstance().getIconHeight() > 0) {
            mParams.height = StatusViewSetting.getInstance().getIconHeight();
        }
        iconNoData.setLayoutParams(mParams);

        TextView mTextView = mViewMap.get(STATUS_NO_DATA).findViewById(R.id.text_no_data);
        if (!TextUtils.isEmpty(StatusViewSetting.getInstance().getNoDataStatus().getNoDataStatusInfo().getStatusText())) {
            mTextView.setText(StatusViewSetting.getInstance().getNoDataStatus().getNoDataStatusInfo().getStatusText());
        }
        if (StatusViewSetting.getInstance().getTextSize() > 0) {
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, StatusViewSetting.getInstance().getTextSize());
        }
        mTextView.setTextColor(StatusViewSetting.getInstance().getTextColor());
    }

    protected void initStatusLoading() {
        ProgressBar progressBar = mViewMap.get(STATUS_LOADING).findViewById(R.id.loading_progress);
        progressBar.setIndeterminate(false);
        if (StatusViewSetting.getInstance().getLoadingStatus() != null
                && StatusViewSetting.getInstance().getLoadingStatus().getLoadingStatusInfo() != null
                && StatusViewSetting.getInstance().getLoadingStatus().getLoadingStatusInfo().getStatusRes() > 0) {
            progressBar.setIndeterminateDrawable(mContext.getResources()
                    .getDrawable(StatusViewSetting.getInstance().getLoadingStatus().getLoadingStatusInfo().getStatusRes()));
        }
    }

    protected void bindButtonBackground(View view) {
        if (StatusBindButtonBackgroundSetting.getInstance().isSetBindButtonBackground()) {
            StateListDrawable stateListDrawable = new StateListDrawable();
//            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new CommonDrawable(mRoot.getContext())
//                    .setIsLeftCircle(true)
//                    .setIsRightCircle(true)
//                    .setGradientStartColor(StatusBindButtonBackgroundSetting.getInstance().getButtonPressGradientStartColor())
//                    .setGradientEndColor(StatusBindButtonBackgroundSetting.getInstance().getButtonPressGradientEndColor()));
//            stateListDrawable.addState(new int[]{}, new CommonDrawable(mRoot.getContext())
//                    .setIsLeftCircle(true)
//                    .setIsRightCircle(true)
//                    .setGradientStartColor(StatusBindButtonBackgroundSetting.getInstance().getButtonNormalGradientStartColor())
//                    .setGradientEndColor(StatusBindButtonBackgroundSetting.getInstance().getButtonNormalGradientEndColor()));
            view.setBackgroundDrawable(stateListDrawable);
        }
    }

    public CommonStatusView setNoNetText(@StringRes int stringId) {
        setNoNetText(mRoot.getResources().getString(stringId));
        return this;
    }

    public CommonStatusView setNoDataText(@StringRes int stringId) {
        setNoDataText(mRoot.getResources().getString(stringId));
        return this;
    }

//    public CommonStatusView setNoLoginText(@StringRes int stringId) {
//        setNoLoginText(mRoot.getResources().getString(stringId));
//        return this;
//    }

    public CommonStatusView setNoNetText(String text) {
        TextView textView = mViewMap.get(STATUS_NO_NET).findViewById(R.id.text_no_net);
        textView.setText(text);
        return this;
    }

//    public void setNoNetImage(int resource) {
//        ImageView imageView = mViewMap.get(STATUS_NO_NET).findViewById(R.id.image_no_net);
//        imageView.setImageResource(resource);
//    }

    public CommonStatusView setNoDataText(CharSequence text) {
        TextView textView = mViewMap.get(STATUS_NO_DATA).findViewById(R.id.text_no_data);
        if (text instanceof SpannableString) {
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        }
        textView.setText(text);
        return this;
    }

    public CommonStatusView setNoDataTextSize(float size) {
        TextView textView = mViewMap.get(STATUS_NO_DATA).findViewById(R.id.text_no_data);
        textView.setTextSize(size);
        return this;
    }

    public CommonStatusView setNoDataTextColor(int color) {
        TextView textView = mViewMap.get(STATUS_NO_DATA).findViewById(R.id.text_no_data);
        textView.setTextColor(color);
        return this;
    }

//    public CommonStatusView setNoLoginText(String text) {
//        TextView textView = mViewMap.get(STATUS_NO_LOGIN).findViewById(R.id.text_no_login);
//        textView.setText(text);
//        return this;
//    }

    public void setLoadFailBackground(int color) {
        FrameLayout frameLayout = mViewMap.get(STATUS_LOAD_FAILED).findViewById(R.id.layout_load_failed);
        frameLayout.setBackgroundColor(color);
    }

    public void setNoNetBackground(int color) {
        LinearLayout linearLayout = mViewMap.get(STATUS_NO_NET).findViewById(R.id.layout_no_net);
        linearLayout.setBackgroundColor(color);
    }

    public void setLoadingBackground(int color) {
        FrameLayout frameLayout = mViewMap.get(STATUS_LOADING).findViewById(R.id.layout_loading);
        frameLayout.setBackgroundColor(color);
    }

    public void setNoNetRootPadding(int left, int top, int right, int bottom) {
        LinearLayout linearLayout = mViewMap.get(STATUS_NO_NET).findViewById(R.id.layout_no_net);
        linearLayout.setPadding(left, top, right, bottom);
    }

    public void setNoDataRootPadding(int left, int top, int right, int bottom) {
        LinearLayout linearLayout = mViewMap.get(STATUS_NO_DATA).findViewById(R.id.layout_no_data);
        linearLayout.setPadding(left, top, right, bottom);
    }

    public void setNoDataImage(int resource) {
        ImageView imageView = mViewMap.get(STATUS_NO_DATA).findViewById(R.id.image_no_data);
        imageView.setImageResource(resource);
    }

    public void setNoDataImageGone() {
        ImageView imageView = mViewMap.get(STATUS_NO_DATA).findViewById(R.id.image_no_data);
        imageView.setVisibility(View.GONE);
    }

    public void setNoDataBackground(int color) {
        LinearLayout linearLayout = mViewMap.get(STATUS_NO_DATA).findViewById(R.id.layout_no_data);
        linearLayout.setBackgroundColor(color);
    }

    public CommonStatusView setOnStateViewClickObserver(final OnStateViewClickObserver onStateViewClickObserver) {
        mOnStateViewClickObserver = onStateViewClickObserver;
        mViewMap.get(STATUS_NO_LOGIN).findViewById(R.id.button_go_to_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStateViewClickObserver.onClickStateView(STATUS_NO_LOGIN);
            }
        });
        mViewMap.get(STATUS_NO_NET).findViewById(R.id.layout_no_net).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStateViewClickObserver.onClickStateView(STATUS_NO_NET);
            }
        });
        mViewMap.get(STATUS_LOAD_FAILED).findViewById(R.id.layout_load_failed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStateViewClickObserver.onClickStateView(STATUS_LOAD_FAILED);
            }
        });
        return this;
    }

    public interface OnStateViewClickObserver {
        void onClickStateView(int state);
    }

}
