package indi.qjx.learnandroid.common.utils

import android.content.Context
import android.content.Intent

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/11/16
 */
fun Context.jumpActivity(context: Context, clazz: Class<*>) {
    val intent = Intent(context, clazz)
    context.startActivity(intent)

}