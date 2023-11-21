package indi.qjx.learnandroid.ipc.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.ipc.service.TCPServerService
import indi.qjx.learnandroid.ipc.utils.MyUtils
import java.io.*
import java.net.Socket
import java.sql.Date
import java.text.SimpleDateFormat

class TCPClientActivity : AppCompatActivity(), View.OnClickListener {
    private val MESSAGE_RECEIVE_NEW_MSG = 1
    private val MESSAGE_SOCKET_CONNECTED = 2

    private var mSendButton: Button? = null
    private var mMessageTextView: TextView? = null
    private var mMessageEditText: EditText? = null

    private var mPrintWriter: PrintWriter? = null
    private var mClientSocket: Socket? = null

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_RECEIVE_NEW_MSG -> {
                    mMessageTextView!!.text = mMessageTextView!!.text.toString() + msg.obj as String
                }
                MESSAGE_SOCKET_CONNECTED -> {
                    mSendButton!!.isEnabled = true
                }
                else -> {}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tcpclient)
        mMessageTextView = findViewById<View>(R.id.msg_container) as TextView
        mSendButton = findViewById<View>(R.id.send) as Button
        mSendButton!!.setOnClickListener(this)
        mMessageEditText = findViewById<View>(R.id.msg) as EditText
        val service = Intent(this, TCPServerService::class.java)
        startService(service)
        object : Thread() {
            override fun run() {
                connectTCPServer()
            }
        }.start()
    }

    override fun onDestroy() {
        if (mClientSocket != null) {
            try {
                mClientSocket!!.shutdownInput()
                mClientSocket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        super.onDestroy()
    }

    override fun onClick(v: View) {
        if (v === mSendButton) {
            val msg = mMessageEditText!!.text.toString()
            if (!TextUtils.isEmpty(msg) && mPrintWriter != null) {
                mMessageEditText!!.setText("")
                val time = formatDateTime(System.currentTimeMillis())
                val showedMsg = "self $time:$msg\n"
                mMessageTextView!!.text = mMessageTextView!!.text.toString() + showedMsg
                object:Thread(){
                    override fun run() {
                        mPrintWriter!!.println(msg)
                    }
                }.start()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatDateTime(time: Long): String {
        return SimpleDateFormat("(HH:mm:ss)").format(Date(time))
    }

    private fun connectTCPServer() {
        var socket: Socket? = null
        while (socket == null) {
            try {
                socket = Socket("localhost", 8688)
                mClientSocket = socket
                mPrintWriter = PrintWriter(
                    BufferedWriter(
                        OutputStreamWriter(socket.getOutputStream())
                    ), true
                )
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED)
                println("connect server success")
            } catch (e: IOException) {
                SystemClock.sleep(1000)
                println("connect tcp server failed, retry...")
            }
        }
        try {
            // 接收服务器端的消息
            val br = BufferedReader(
                InputStreamReader(
                    socket.getInputStream()
                )
            )
            while (!this@TCPClientActivity.isFinishing) {
                val msg = br.readLine()
                println("receive :$msg")
                if (msg != null) {
                    val time = formatDateTime(System.currentTimeMillis())
                    val showedMsg = """
                    server $time:$msg
                    
                    """.trimIndent()
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG, showedMsg)
                        .sendToTarget()
                }
            }
            println("quit...")
            MyUtils.close(mPrintWriter)
            MyUtils.close(br)
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}