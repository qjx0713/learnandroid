package indi.qjx.learnandroid.database.ui.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.common.adapter.SimpleBtnAdapter
import indi.qjx.learnandroid.database.helper.MyDatabaseHelper
import indi.qjx.learnandroid.databinding.ActivitySqliteBinding
import indi.qjx.learnandroid.model.BtnAction
import indi.qjx.base.mvvm.BaseMvvmActivity
import indi.qjx.base.mvvm.viewmodel.BaseViewModel

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/12/01
 */
class SQLiteActivity : BaseMvvmActivity<BaseViewModel, ActivitySqliteBinding>() {

    private val TAG = this::class.java.simpleName

    lateinit var dbHelper: MyDatabaseHelper

    private val functions = listOf<BtnAction>(
        BtnAction("创建数据库") {
            dbHelper.writableDatabase
        },
        BtnAction("添加数据") {
            addData()
        },
        BtnAction("更新数据") {
            updateData()
        },
        BtnAction("删除数据") {
            deleteData()
        },
        BtnAction("查询数据") {
            queryData()
        },
        BtnAction("使用事务") {
            replaceData()
        },

        )

    private fun replaceData() {
        val db = dbHelper.writableDatabase
        db.beginTransaction() // 开启事务
        try {
            db.delete("Book", null, null)
//                if (true) {
//                    // 在这里手动抛出一个异常，让事务失败
//                    throw NullPointerException()
//                }
            val values = ContentValues().apply {
                put("name", "Game of Thrones")
                put("author", "George Martin")
                put("pages", 720)
                put("price", 20.85)
            }
            db.insert("Book", null, values)
            db.setTransactionSuccessful() // 事务已经执行成功
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction() // 结束事务
        }
    }

    @SuppressLint("Range")
    private fun queryData() {
        val db = dbHelper.writableDatabase
        // 查询Book表中所有的数据
        val cursor = db.query("Book", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                val name = cursor.getString(cursor.getColumnIndex("name"))
                val author = cursor.getString(cursor.getColumnIndex("author"))
                val pages = cursor.getInt(cursor.getColumnIndex("pages"))
                val price = cursor.getDouble(cursor.getColumnIndex("price"))
                Log.d(TAG, "book name is $name")
                Log.d(TAG, "book author is $author")
                Log.d(TAG, "book pages is $pages")
                Log.d(TAG, "book price is $price")
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    private fun deleteData() {
        val db = dbHelper.writableDatabase
        db.delete("Book", "pages > ?", arrayOf("500"))
    }

    private fun updateData() {
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put("price", 10.99)
        val rows = db.update("Book", values, "name = ?", arrayOf("The Da Vinci Code"))
        Toast.makeText(this, "rows is $rows", Toast.LENGTH_SHORT).show()
    }

    private fun addData() {
        val db = dbHelper.writableDatabase
        val values1 = ContentValues().apply {
            // 开始组装第一条数据
            put("name", "The Da Vinci Code")
            put("author", "Dan Brown")
            put("pages", 454)
            put("price", 16.96)
        }
        db.insert("Book", null, values1) // 插入第一条数据
        val values2 = ContentValues().apply {
            // 开始组装第二条数据
            put("name", "The Lost Symbol")
            put("author", "Dan Brown")
            put("pages", 510)
            put("price", 19.95)
        }
        db.insert("Book", null, values2) // 插入第二条数据
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_sqlite
    }

    override fun bindViewModel() {

    }

    override fun init() {
        title = this::class.java.simpleName
        val simpleBtnAdapter = SimpleBtnAdapter()
        simpleBtnAdapter.setNewData(functions)
        mViewDataBind.recyclerview.apply {
            layoutManager =
                LinearLayoutManager(
                    this@SQLiteActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            adapter = simpleBtnAdapter
        }

        dbHelper = MyDatabaseHelper(this, "BookStore.db", 1)
    }

}