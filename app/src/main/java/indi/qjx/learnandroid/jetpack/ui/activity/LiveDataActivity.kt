package indi.qjx.learnandroid.jetpack.ui.activity

import android.util.Log
import androidx.lifecycle.*
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.databinding.ActivityLivedataBinding
import indi.qjx.learnandroid.jetpack.viewmodel.LiveDataVM
import indi.qjx.base.mvvm.BaseMvvmActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/11/27
 */
class LiveDataActivity  : BaseMvvmActivity<LiveDataVM, ActivityLivedataBinding>() {

    private val TAG = this::class.java.simpleName

    override fun getLayoutId(): Int {
        return R.layout.activity_livedata
    }

    override fun bindViewModel() {
        mViewDataBind.addObserver.setOnClickListener {
            //增加一个观察者
            mViewModel.data.observe(this, {
                Log.d(TAG, "observe2: data=$it")
            })
        }
        mViewDataBind.add.setOnClickListener {
            mViewModel.add()
//            mViewModel.reNotify()

        }
        mViewModel.data.observe(this, {
            GlobalScope.launch {
                delay(5000)
                Log.d(TAG, "observe1: data=$it")
            }

        })

    }

    override fun init() {
        title =  this::class.java.simpleName
    }
}