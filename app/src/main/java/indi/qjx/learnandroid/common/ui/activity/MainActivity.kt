package indi.qjx.learnandroid.common.ui.activity

import androidx.recyclerview.widget.LinearLayoutManager
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.common.adapter.SimpleBtnAdapter
import indi.qjx.learnandroid.databinding.ActivityMainBinding
import indi.qjx.learnandroid.model.BtnAction
import indi.qjx.learnandroid.ipc.ui.activity.IPCActivity
import indi.qjx.learnandroid.common.utils.jumpActivity
import indi.qjx.libtemplate.mvvm.BaseMvvmActivity
import indi.qjx.libtemplate.mvvm.viewmodel.BaseViewModel

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/11/16
 */
class MainActivity : BaseMvvmActivity<BaseViewModel, ActivityMainBinding>() {
    private val TAG = this::class.java.simpleName

    private val functions = listOf<BtnAction>(
        BtnAction("跨进程通信IPC") {
            jumpActivity(this, IPCActivity::class.java)
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