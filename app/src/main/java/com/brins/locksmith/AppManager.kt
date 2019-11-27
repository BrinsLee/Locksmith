package com.brins.locksmith

import android.app.Activity
import java.util.*

class AppManager private constructor(){

    companion object{
        private var activityStack: Stack<Activity>? = null

        class SingletonHolder {
            companion object {
                val Instance = AppManager()
            }
        }

        fun getAppManager() = SingletonHolder.Instance
    }

    fun addActivity(activity: Activity) {
        if (activityStack == null) {
            activityStack = Stack()
        }
        activityStack!!.add(activity)
    }

    fun removeActivity(activity: Activity?) {
        if (activity != null) {
            activityStack!!.remove(activity)
        }
    }

    fun isHasActivity(): Boolean {
        return if (activityStack != null) {
            !activityStack!!.isEmpty()
        } else false
    }

    fun finishActivity(activity: Activity?) {
        if (activity != null) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
    }

    fun getActivity(cls: Class<*>): Activity? {
        if (activityStack != null)
            for (activity in activityStack!!) {
                if (activity.javaClass == cls) {
                    return activity
                }
            }
        return null
    }
}