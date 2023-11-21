package indi.qjx.learnandroid.view.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import indi.qjx.learnandroid.R

class ViewSimpleActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_simple)

        val textView = findViewById<TextView>(R.id.tv_test)

        val button = findViewById<TextView>(R.id.btn_test)
        Log.d(
            TAG,
            "textView x = ${textView.x},y = ${textView.y},scrollx = ${textView.scrollX} ,scrolly = ${textView.scrollY}"
        )
        button.setOnClickListener {
            //使用scrollBy移动，移动的是view的内容，文字会向左上移动
            textView.scrollBy(10, 10)
            Log.d(
                TAG,
                "textView x = ${textView.x},y = ${textView.y},scrollx = ${textView.scrollX} ,scrolly = ${textView.scrollY}"
            )
        }
    }
}