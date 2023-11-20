package indi.qjx.learnandroid.ipc.provider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/11/20
 */
class DbOpenHelper(context: Context?):SQLiteOpenHelper(context,
    DB_NAME, null, DB_VERSION) {

    companion object {
        private val DB_NAME = "book_provider.db"
        val BOOK_TABLE_NAME = "book"
        val USER_TALBE_NAME = "user"
        private val DB_VERSION = 3
    }

    private val CREATE_BOOK_TABLE = ("CREATE TABLE IF NOT EXISTS "
            + BOOK_TABLE_NAME + "(_id INTEGER PRIMARY KEY," + "name TEXT)")

    private val CREATE_USER_TABLE = ("CREATE TABLE IF NOT EXISTS "
            + USER_TALBE_NAME + "(_id INTEGER PRIMARY KEY," + "name TEXT,"
            + "sex INT)")

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_BOOK_TABLE)
        db.execSQL(CREATE_USER_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // TODO ignored
    }
}