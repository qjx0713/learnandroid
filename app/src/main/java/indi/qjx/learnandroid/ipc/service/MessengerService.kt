package indi.qjx.learnandroid.ipc.service

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import indi.qjx.learnandroid.ipc.consts.MyConsts

class MessengerService : Service() {

    companion object {
        private val TAG = "MessengerService"
    }

    private class MessengerHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MyConsts.MSG_FROM_CLIENT -> {
                    Log.i(TAG, "receive msg from Client:" + msg.data.getString("msg"))
                    val client = msg.replyTo
                    val relpyMessage: Message = Message.obtain(null, MyConsts.MSG_FROM_SERVICE)
                    val bundle = Bundle()
                    bundle.putString("reply", "嗯，你的消息我已经收到，稍后会回复你。")
                    relpyMessage.data = bundle
                    try {
                        client.send(relpyMessage)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private val mMessenger = Messenger(MessengerHandler())

    override fun onBind(intent: Intent?): IBinder? {
        return mMessenger.binder
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

}