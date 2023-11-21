package indi.qjx.learnandroid.ipc.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import indi.qjx.learnandroid.ipc.utils.MyUtils
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class TCPServerService : Service() {
    private var mIsServiceDestoryed = false
    private val mDefinedMessages = arrayOf(
        "你好啊，哈哈",
        "请问你叫什么名字呀？",
        "今天北京天气不错啊，shy",
        "你知道吗？我可是可以和多个人同时聊天的哦",
        "给你讲个笑话吧：据说爱笑的人运气不会太差，不知道真假。"
    )

    override fun onCreate() {
        Thread(TcpServer()).start()
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        mIsServiceDestoryed = true
        super.onDestroy()
    }

    inner class TcpServer : Runnable {
        override fun run() {
            var serverSocket: ServerSocket? = try {
                ServerSocket(8688)
            } catch (e: IOException) {
                System.err.println("establish tcp server failed, port:8688")
                e.printStackTrace()
                return
            }
            while (!mIsServiceDestoryed) {
                try {
                    // 接受客户端请求
                    val client = serverSocket?.accept()
                    println("accept")
                    object : Thread() {
                        override fun run() {
                            try {
                                if (client != null) {
                                    responseClient(client)
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun responseClient(client: Socket) {
        // 用于接收客户端消息
        val `in` = BufferedReader(
            InputStreamReader(
                client.getInputStream()
            )
        )
        // 用于向客户端发送消息
        val out = PrintWriter(
            BufferedWriter(
                OutputStreamWriter(client.getOutputStream())
            ), true
        )
        out.println("欢迎来到聊天室！")
        while (!mIsServiceDestoryed) {
            val str = `in`.readLine()
            println("msg from client:$str")
            if (str == null) {
                break
            }
            val i = Random().nextInt(mDefinedMessages.size)
            val msg = mDefinedMessages[i]
            out.println(msg)
            println("send :$msg")
        }
        println("client quit.")
        // 关闭流
        MyUtils.close(out)
        MyUtils.close(`in`)
        client.close()
    }
}