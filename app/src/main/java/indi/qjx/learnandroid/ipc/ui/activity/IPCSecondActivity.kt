package indi.qjx.learnandroid.ipc.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import indi.qjx.learnandroid.R

class IPCSecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ipcsecond)
        title =  this::class.java.simpleName
    }
}