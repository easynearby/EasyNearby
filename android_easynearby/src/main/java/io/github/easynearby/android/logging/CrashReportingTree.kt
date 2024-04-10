package io.github.easynearby.android.logging

import android.annotation.SuppressLint
import android.util.Log
import timber.log.Timber


/** A tree which logs important information for crash reporting.  */
internal class CrashReportingTree : Timber.Tree() {
    @SuppressLint("LogNotTimber")
    protected override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        if (t != null) {
            if (priority == Log.ERROR) {
                Log.e(tag, message, t)
            } else if (priority == Log.WARN) {
                Log.w(tag, message, t)
            }
        }
    }
}