package indi.qjx.learnandroid.ipc.ui.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.os.IBinder.DeathRecipient
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.ipc.aidl.IBookManager
import indi.qjx.learnandroid.ipc.aidl.Book
import indi.qjx.learnandroid.ipc.aidl.IOnNewBookArrivedListener
import indi.qjx.learnandroid.ipc.service.BookManagerService

class BookManagerActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        private val TAG = "BookManagerActivity"

        private const val MESSAGE_NEW_BOOK_ARRIVED = 1
    }

    private var mRemoteBookManager :IBookManager? = null
    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_NEW_BOOK_ARRIVED -> Log.d(
                    TAG,
                    "receive new book :" + msg.obj
                )
                else -> super.handleMessage(msg)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_manager)
        val intent = Intent(this, BookManagerService::class.java)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val bookManager: IBookManager = IBookManager.Stub.asInterface(service)
            mRemoteBookManager = bookManager
            try {
                mRemoteBookManager!!.asBinder().linkToDeath(mDeathRecipient, 0)
                val list: List<Book> = bookManager.bookList
                Log.i(TAG, "query book list, list type:" + list.javaClass.canonicalName)
                Log.i(TAG, "query book list:$list")
                val newBook = Book(3, "Android进阶")
                bookManager.addBook(newBook)
                Log.i(TAG, "add book:$newBook")
                val newList: List<Book> = bookManager.bookList
                bookManager.registerListener(mOnNewBookArrivedListener)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.d(TAG, "onServiceDisconnected. tname:" + Thread.currentThread().name)
        }
    }

    private val mDeathRecipient: DeathRecipient = object : DeathRecipient {
        override fun binderDied() {
            Log.d(TAG, "binder died. tname:" + Thread.currentThread().name)
            if (mRemoteBookManager == null) return
            mRemoteBookManager!!.asBinder().unlinkToDeath(this, 0)
            mRemoteBookManager = null
            // TODO:这里重新绑定远程Service
        }
    }

    private val mOnNewBookArrivedListener: IOnNewBookArrivedListener = object : IOnNewBookArrivedListener.Stub() {

        override fun OnNewBookArrived(newBook: Book?) {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook)
                .sendToTarget()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mRemoteBookManager?.asBinder()?.isBinderAlive == true) {
            mRemoteBookManager?.unregisterListener(mOnNewBookArrivedListener)
        }
        unbindService(mConnection)
    }
}