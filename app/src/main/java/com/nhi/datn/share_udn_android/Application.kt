package com.nhi.datn.share_udn_android

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import java.util.*

class BartrApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks {

    private val appId = "0EA84E01-B652-42E0-B213-18003575A22B"
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    private var mTimeSentToBackground: Long = 0

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()

    }

    companion object {
        fun isActivityVisible(): Boolean {
            return activityVisible
        }

        fun setActivityVisible(isActive: Boolean) {
            activityVisible = isActive
        }

        private var activityVisible: Boolean = true
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        this.onConnectSendBird()
    }

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations && !BartrApplication.isActivityVisible()) {
            /*App in foreground*/
            val currentTime = Calendar.getInstance().timeInMillis
            if (currentTime - mTimeSentToBackground > 5 * 60 * 1000) {
                activity?.startActivity(Intent(activity, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            }
            BartrApplication.setActivityVisible(true)

            // Connect sendbird
            this.onConnectSendBird()
        }
    }

    override fun onActivityDestroyed(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
        isActivityChangingConfigurations = activity?.isChangingConfigurations ?: false
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            /*App in background*/
            BartrApplication.setActivityVisible(false)
            mTimeSentToBackground = Calendar.getInstance().timeInMillis
        }
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
    }

    private fun onConnectSendBird() {
    }
}
