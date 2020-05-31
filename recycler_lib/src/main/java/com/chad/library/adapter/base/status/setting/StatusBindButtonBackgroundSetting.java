package com.chad.library.adapter.base.status.setting;

import android.graphics.Color;

/**
 * @author dengjiaming
 * @date 2018/11/20.
 */
public class StatusBindButtonBackgroundSetting {

    private volatile static StatusBindButtonBackgroundSetting sInstance;
    private boolean mSetBindButtonBackground = false;
    private int buttonPressGradientStartColor = Color.parseColor("#00a8ff");
    private int buttonPressGradientEndColor = Color.parseColor("#a860f4");
    private int buttonNormalGradientStartColor = Color.parseColor("#796df5");
    private int buttonNormalGradientEndColor = Color.parseColor("#bf7af7");

    private StatusBindButtonBackgroundSetting() {
    }

    public static StatusBindButtonBackgroundSetting getInstance() {
        if (sInstance == null) {
            synchronized (StatusBindButtonBackgroundSetting.class) {
                if (sInstance == null) {
                    sInstance = new StatusBindButtonBackgroundSetting();
                }
            }
        }
        return sInstance;
    }

    public boolean isSetBindButtonBackground() {
        return mSetBindButtonBackground;
    }

    public StatusBindButtonBackgroundSetting setSetBindButtonBackground(boolean setBindButtonBackground) {
        mSetBindButtonBackground = setBindButtonBackground;
        return this;
    }

    public int getButtonPressGradientStartColor() {
        return buttonPressGradientStartColor;
    }

    public StatusBindButtonBackgroundSetting setButtonPressGradientStartColor(int buttonPressGradientStartColor) {
        this.buttonPressGradientStartColor = buttonPressGradientStartColor;
        return this;
    }

    public int getButtonPressGradientEndColor() {
        return buttonPressGradientEndColor;
    }

    public StatusBindButtonBackgroundSetting setButtonPressGradientEndColor(int buttonPressGradientEndColor) {
        this.buttonPressGradientEndColor = buttonPressGradientEndColor;
        return this;
    }

    public int getButtonNormalGradientStartColor() {
        return buttonNormalGradientStartColor;
    }

    public StatusBindButtonBackgroundSetting setButtonNormalGradientStartColor(int buttonNormalGradientStartColor) {
        this.buttonNormalGradientStartColor = buttonNormalGradientStartColor;
        return this;
    }

    public int getButtonNormalGradientEndColor() {
        return buttonNormalGradientEndColor;
    }

    public StatusBindButtonBackgroundSetting setButtonNormalGradientEndColor(int buttonNormalGradientEndColor) {
        this.buttonNormalGradientEndColor = buttonNormalGradientEndColor;
        return this;
    }
}
