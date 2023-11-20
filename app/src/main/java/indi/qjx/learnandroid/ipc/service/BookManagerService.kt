package indi.qjx.learnandroid.ipc.service

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import indi.qjx.learnandroid.ipc.aidl.IBookManager
import indi.qjx.learnandroid.ipc.aidl.Book
import indi.qjx.learnandroid.ipc.aidl.IOnNewBookArrivedListener
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class BookManagerService : Service() {

    companion object {
        private val TAG = "BMS"
    }

    private val mIsServiceDestoryed = AtomicBoolean(false)

    private val mBookList = CopyOnWriteArrayList<Book>()
//     private val  mListenerList = CopyOnWriteArrayList<IOnNewBookArrivedListener>();

    // private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListenerList =
    // new CopyOnWriteArrayList<IOnNewBookArrivedListener>();
    private val mListenerList = RemoteCallbackList<IOnNewBookArrivedListener>()

    private val mBinder: Binder = object : IBookManager.Stub() {

        override fun getBookList(): MutableList<Book> {
            return mBookList
        }

        @Throws(RemoteException::class)
        override fun addBook(book: Book) {
            mBookList.add(book)
        }

        @Throws(RemoteException::class)
        override fun registerListener(listener: IOnNewBookArrivedListener) {
            mListenerList.register(listener)
            val N = mListenerList.beginBroadcast()
            mListenerList.finishBroadcast()
            Log.d(TAG, "registerListener, current size:$N")
        }

        @Throws(RemoteException::class)
        override fun unregisterListener(listener: IOnNewBookArrivedListener) {
            mListenerList.unregister(listener)
            //beginBroadcast和finishBroadcast配对使用
            val N = mListenerList.beginBroadcast()
            mListenerList.finishBroadcast()
            Log.d(TAG, "unregisterListener, current size:$N")
        }
    }

    override fun onCreate() {
        super.onCreate()
        mBookList.add(Book(1, "Android"))
        mBookList.add(Book(2, "Ios"))
        Thread(ServiceWorker()).start()
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onDestroy() {
        mIsServiceDestoryed.set(true)
        super.onDestroy()
    }

    @Throws(RemoteException::class)
    private fun onNewBookArrived(book: Book) {
        mBookList.add(book)
        val N = mListenerList.beginBroadcast()
        for (i in 0 until N) {
            val l = mListenerList.getBroadcastItem(i)
            if (l != null) {
                try {
                    l.OnNewBookArrived(book)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }
        mListenerList.finishBroadcast()
    }

    inner class ServiceWorker : Runnable {
        override fun run() {
            // do background processing here.....

                try {
                    Thread.sleep(5000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                val bookId: Int = mBookList.size + 1
                val newBook = Book(bookId, "new book#$bookId")
                try {
                    onNewBookArrived(newBook)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

        }
    }
}