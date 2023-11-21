package indi.qjx.learnandroid.ipc.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import indi.qjx.learnandroid.ipc.utils.BinderPool

class BinderPoolService : Service() {


    private val TAG = "BinderPoolService"

    private val mBinderPool: Binder = BinderPool.BinderPoolImpl()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return mBinderPool
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}