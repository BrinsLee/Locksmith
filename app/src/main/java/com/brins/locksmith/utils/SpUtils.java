package com.brins.locksmith.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.brins.locksmith.BaseApplication;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * SharedPreferences工具
 */
public class SpUtils {
    private static final String DEFAULT_FILE_NAME = "commercialize";

    private static Map<String, SoftReference<SharedPreferences>> sMap = new HashMap<String, SoftReference<SharedPreferences>>();

    private SharedPreferences mSharedPreferences;

    private SpUtils(SharedPreferences mSharedPreferences) {
        this.mSharedPreferences = mSharedPreferences;
    }

    /**
     * 获取SpUtils对象
     *
     * @param spName
     * @return
     */
    public static SpUtils obtain(String spName) {
        SharedPreferences sharedPreferences = null;
        if (sMap.containsKey(spName)) {
            SoftReference<SharedPreferences> softReference = sMap.get(spName);
            if (softReference != null) {
                sharedPreferences = softReference.get();
            }
        }

        if (null == sharedPreferences) {
            sharedPreferences = BaseApplication.context.getSharedPreferences(spName, Context.MODE_PRIVATE);
            sMap.put(spName, new SoftReference<SharedPreferences>(sharedPreferences));
        }
        return new SpUtils(sharedPreferences);
    }

    public static SpUtils obtain() {
        return obtain(DEFAULT_FILE_NAME);
    }

    public static SharedPreferences getSharedPreferences(String spName) {
        return BaseApplication.context.getSharedPreferences(spName, Context.MODE_MULTI_PROCESS);
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        return mSharedPreferences.edit();
    }

    public SpUtils put(SharedPreferences.Editor editor, String key, boolean value) {
        editor.putBoolean(key, value);
        return this;
    }

    public SpUtils put(SharedPreferences.Editor editor, String key, int value) {
        editor.putInt(key, value);
        return this;
    }

    public SpUtils put(SharedPreferences.Editor editor, String key, float value) {
        editor.putFloat(key, value);
        return this;
    }

    public SpUtils put(SharedPreferences.Editor editor, String key, long value) {
        editor.putLong(key, value);
        return this;
    }

    public SpUtils put(SharedPreferences.Editor editor, String key, String value) {
        editor.putString(key, value);
        return this;
    }

    public SpUtils put(SharedPreferences.Editor editor, String key, Set<String> value) {
        editor.putStringSet(key, value);
        return this;
    }

    public void apply(SharedPreferences.Editor editor) {
        editor.apply();
    }

    public void commit(SharedPreferences.Editor editor) {
        editor.commit();
    }


    public void save(String key, boolean value) {
        getEditor().putBoolean(key, value).apply();
    }

    public void saveAsyn(String key, boolean value) {
        getEditor().putBoolean(key, value).commit();
    }

    public void save(String key, int value) {
        getEditor().putInt(key, value).apply();
    }

    public void saveAsyn(String key, int value) {
        getEditor().putInt(key, value).commit();
    }

    public void save(String key, long value) {
        getEditor().putLong(key, value).apply();
    }

    public void saveAsyn(String key, long value) {
        getEditor().putLong(key, value).commit();
    }

    public void save(String key, String value) {
        getEditor().putString(key, value).apply();
    }

    public void saveAsyn(String key, String value) {
        getEditor().putString(key, value).commit();
    }

    public void save(String key, float value) {
        getEditor().putFloat(key, value).apply();
    }

    public void saveAsyn(String key, float value) {
        getEditor().putFloat(key, value).commit();
    }

    public void remove(String... keys) {
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                getEditor().remove(key);
            }
            getEditor().apply();
        }
    }

    public void removeAsyn(String... keys) {
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                getEditor().remove(key);
            }
            getEditor().commit();
        }
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mSharedPreferences.getBoolean(key, defaultValue);
    }

    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int def) {
        return mSharedPreferences.getInt(key, def);
    }

    public long getLong(String key) {
        return getLong(key, -1);
    }

    public long getLong(String key, long def) {
        return mSharedPreferences.getLong(key, def);
    }

    public float getFloat(String key) {
        return getFloat(key, -1);
    }

    public float getFloat(String key, float def) {
        return mSharedPreferences.getFloat(key, def);
    }

    public String get(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public boolean get(String key, boolean defaultValue) {
        return mSharedPreferences.getBoolean(key, defaultValue);
    }

    public int get(String key, int def) {
        return mSharedPreferences.getInt(key, def);
    }

    public long get(String key, long def) {
        return mSharedPreferences.getLong(key, def);
    }

    public float get(String key, float def) {
        return mSharedPreferences.getFloat(key, def);
    }

    public Set<String> get(String key, Set<String> def) {
        return mSharedPreferences.getStringSet(key, def);
    }

    public void save(String key, Set<String> value) {
        getEditor().putStringSet(key, value).apply();
    }

    public void saveAsyn(String key, Set<String> value) {
        getEditor().putStringSet(key, value).commit();
    }

    public Set<String> getStringSet(String key, Set<String> def) {
        return mSharedPreferences.getStringSet(key, def);
    }
}
