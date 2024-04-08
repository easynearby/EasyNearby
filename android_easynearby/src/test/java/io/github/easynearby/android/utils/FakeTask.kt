package io.github.easynearby.android.utils

import android.app.Activity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import java.util.concurrent.Executor

class FakeTask<V> : Task<V>() {

    private var failureListener: OnFailureListener? = null
    private var successListener: OnSuccessListener<in V>? = null

    fun invokeSuccessListener(value: V) {
        successListener?.onSuccess(value)
    }

    fun invokeFailureListener(exception: Exception) {
        failureListener?.onFailure(exception)
    }

    override fun addOnFailureListener(p0: OnFailureListener): Task<V> {
        failureListener = p0
        return this
    }

    override fun addOnFailureListener(p0: Activity, p1: OnFailureListener): Task<V> {
        TODO("Not yet implemented")
    }

    override fun addOnFailureListener(p0: Executor, p1: OnFailureListener): Task<V> {
        TODO("Not yet implemented")
    }

    override fun addOnSuccessListener(p0: OnSuccessListener<in V>): Task<V> {
        successListener = p0
        return this
    }

    override fun addOnSuccessListener(p0: Activity, p1: OnSuccessListener<in V>): Task<V> {
        TODO("Not yet implemented")
    }

    override fun addOnSuccessListener(p0: Executor, p1: OnSuccessListener<in V>): Task<V> {
        TODO("Not yet implemented")
    }

    override fun getException(): Exception? {
        TODO("Not yet implemented")
    }

    override fun getResult(): V {
        TODO("Not yet implemented")
    }

    override fun <X : Throwable?> getResult(p0: Class<X>): V {
        TODO("Not yet implemented")
    }

    override fun isCanceled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isComplete(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSuccessful(): Boolean {
        TODO("Not yet implemented")
    }
}