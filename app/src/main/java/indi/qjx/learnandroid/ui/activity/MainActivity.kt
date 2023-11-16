package indi.qjx.learnandroid.ui.activity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.adapter.SimpleBtnAdapter
import indi.qjx.learnandroid.databinding.ActivityMainBinding
import indi.qjx.learnandroid.model.BtnAction
import indi.qjx.learnandroid.utils.jumpActivity
import indi.qjx.libtemplate.mvvm.BaseMvvmActivity
import indi.qjx.libtemplate.mvvm.viewmodel.BaseViewModel

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/11/16
 */
class MainActivity : BaseMvvmActivity<BaseViewModel, ActivityMainBinding>() {

    private val functions = listOf<BtnAction>(
        BtnAction("AIDL") {
            jumpActivity(this, MainActivity::class.java)
        },
        BtnAction("四大组件") {
            jumpActivity(this, MainActivity::class.java)
        },
        BtnAction("四大组件") {
            jumpActivity(this, MainActivity::class.java)
        },
        BtnAction("四大组件") {
            jumpActivity(this, MainActivity::class.java)
        },
        BtnAction("四大组件") {
            jumpActivity(this, MainActivity::class.java)
        },
        BtnAction("四大组件") {
            jumpActivity(this, MainActivity::class.java)
        },
        BtnAction("四大组件") {
            jumpActivity(this, MainActivity::class.java)
        },
        BtnAction("四大组件") {
            jumpActivity(this, MainActivity::class.java)
        },
        BtnAction("四大组件") {
            jumpActivity(this, MainActivity::class.java)
        },
        BtnAction("四大组件") {
            jumpActivity(this, MainActivity::class.java)
        },
        BtnAction("四大组件") {
            jumpActivity(this, MainActivity::class.java)
        },
    )

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun bindViewModel() {

    }

    override fun init() {
        val simpleBtnAdapter = SimpleBtnAdapter()
        simpleBtnAdapter.setNewData(functions)
        mViewDataBind.recyclerview.apply {
            layoutManager =
                LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            adapter = simpleBtnAdapter
        }
    }
}