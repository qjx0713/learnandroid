package indi.qjx.learnandroid.ipc.ui.activity

import android.content.ContentValues
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.ipc.aidl.Book

class ProviderActivity : AppCompatActivity() {

    private val TAG = "ProviderActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider)
//         val uri = Uri.parse("content://indi.qjx.learnandroid.ipc.provider");
//         contentResolver.query(uri, null, null, null, null);
//         contentResolver.query(uri, null, null, null, null);
//         contentResolver.query(uri, null, null, null, null);
        val bookUri = Uri.parse("content://indi.qjx.learnandroid.ipc.provider/book")
        val values = ContentValues()
        values.put("_id", 6)
        values.put("name", "程序设计的艺术")
        contentResolver.insert(bookUri, values)
        val bookCursor = contentResolver.query(bookUri, arrayOf("_id", "name"), null, null, null)
        while (bookCursor!!.moveToNext()) {
            val book = Book()
            book.bookId = bookCursor.getInt(0)
            book.bookName = bookCursor.getString(1)
            Log.d(TAG, "query book:" + book.toString())
        }
        bookCursor.close()
//        val userUri = Uri.parse("content://indi.qjx.learnandroid.ipc.provider/user")
//        val userCursor =
//            contentResolver.query(userUri, arrayOf("_id", "name", "sex"), null, null, null)
//        while (userCursor!!.moveToNext()) {
//            val user = User()
//            user.userId = userCursor.getInt(0)
//            user.userName = userCursor.getString(1)
//            user.isMale = userCursor.getInt(2) == 1
//            Log.d(TAG, "query user:" + user.toString())
//        }
//        userCursor.close()
    }
}