package indi.qjx.learnandroid.ipc.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.ipc.consts.MyConsts
import indi.qjx.learnandroid.ipc.service.MessengerService

class MessengerActivity : AppCompatActivity() {

    companion object {
        private val TAG = "MessengerActivity"
    }

    private var mService: Messenger? = null
    private val mGetReplyMessenger = Messenger(MessengerHandler())

    private class MessengerHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MyConsts.MSG_FROM_SERVICE -> Log.i(
                    TAG,
                    "receive msg from Service:" + msg.data.getString("reply")
                )
                else -> super.handleMessage(msg)
            }
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = Messenger(service)
            Log.d(TAG, "bind service")
            val msg: Message = Message.obtain(null, MyConsts.MSG_FROM_CLIENT)
            val data = Bundle()
            data.putString("msg", "hello, this is client.")
            msg.data = data
            //需要把接收服务端回复的Messenger通过Message的replyTo参数传给服务器
            msg.replyTo = mGetReplyMessenger
            try {
                mService!!.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger)
        val intent = Intent(this, MessengerService::class.java)
        bindService(intent, mConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        unbindService(mConnection)
        super.onDestroy()
    }
}