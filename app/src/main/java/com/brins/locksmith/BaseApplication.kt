package com.brins.locksmith

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle

class BaseApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        context = this
        setApplication(this)
    }


    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context : Context

        fun setApplication(baseApplication: BaseApplication) {
            baseApplication.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks{
                override fun onActivityPaused(activity: Activity?) {

                }

                override fun onActivityResumed(activity: Activity?) {
                }

                override fun onActivityStarted(activity: Activity) {
                    AppManager.getAppManager().addActivity(activity)
                }

                override fun onActivityDestroyed(activity: Activity?) {
                }

                override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
                }

                override fun onActivityStopped(activity: Activity) {
                    AppManager.getAppManager().removeActivity(activity)
                }

                override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {

                }

            })
        }
    }
}