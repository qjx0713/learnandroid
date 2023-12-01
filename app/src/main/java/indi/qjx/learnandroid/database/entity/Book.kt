package indi.qjx.learnandroid.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/12/01
 */
@Entity
data class Book(var name: String, var pages: Int, var author: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}