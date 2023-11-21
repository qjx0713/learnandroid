package indi.qjx.learnandroid.ipc.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.ipc.aidl.ICompute
import indi.qjx.learnandroid.ipc.aidl.ISecurityCenter
import indi.qjx.learnandroid.ipc.impl.ComputeImpl
import indi.qjx.learnandroid.ipc.impl.SecurityCenterImpl
import indi.qjx.learnandroid.ipc.utils.BinderPool

class BinderPoolActivity : AppCompatActivity() {
    private val TAG = "BinderPoolActivity"

    private var mSecurityCenter: ISecurityCenter? = null
    private var mCompute: ICompute? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_binder_pool)
        Thread { doWork() }.start()
    }

    private fun doWork() {
        val binderPool: BinderPool = BinderPool.getInsance(this@BinderPoolActivity)
        val securityBinder: IBinder = binderPool
            .queryBinder(BinderPool.BINDER_SECURITY_CENTER)
        mSecurityCenter = ISecurityCenter.Stub
            .asInterface(securityBinder)
        Log.d(TAG, "visit ISecurityCenter")
        val msg = "helloworld-安卓"
        println("content:$msg")
        try {
            val password: String = mSecurityCenter!!.encrypt(msg)
            println("encrypt:$password")
            println("decrypt:" + mSecurityCenter!!.decrypt(password))
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        Log.d(TAG, "visit ICompute")
        val computeBinder: IBinder = binderPool
            .queryBinder(BinderPool.BINDER_COMPUTE)
        mCompute = ICompute.Stub.asInterface(computeBinder)
        try {
            println("3+5=" + mCompute!!.add(3, 5))
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
}