package com.brins.locksmith.ui.activity

import android.app.Activity
import java.util.*

/**
 * @author lipeilin
 * @date 2020/4/15
 */
class ActivityCollector {
    companion object {
        @JvmStatic
        var activityList: MutableList<Activity> = ArrayList()

        @JvmStatic
        fun addActivity(activity: Activity) {
            activityList.add(activity)
        }

        @JvmStatic
        fun removeActivity(activity: Activity) {
            activityList.remove(activity)
        }

        @JvmStatic
        open fun finishall() {
            for (activity in activityList) {
                if (!activity.isFinishing) {
                    activity.finish()
                }
            }
            activityList.clear()
        }

        @JvmStatic
        fun removeActivities(num: Int) {
            for (i in 0 until num) {
                if (!activityList[i].isFinishing) {
                    activityList[i].finish()
                }
            }
        }

    }
}