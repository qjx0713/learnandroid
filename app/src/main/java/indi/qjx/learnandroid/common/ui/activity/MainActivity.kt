package indi.qjx.learnandroid.common.ui.activity

import androidx.recyclerview.widget.LinearLayoutManager
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.common.adapter.SimpleBtnAdapter
import indi.qjx.learnandroid.databinding.ActivityMainBinding
import indi.qjx.learnandroid.model.BtnAction
import indi.qjx.learnandroid.ipc.ui.activity.IPCActivity
import indi.qjx.learnandroid.common.utils.jumpActivity
import indi.qjx.learnandroid.database.ui.activity.DatabaseActivity
import indi.qjx.learnandroid.flow.ui.activity.FlowActivity
import indi.qjx.learnandroid.jetpack.ui.activity.JetPackActivity
import indi.qjx.learnandroid.view.ui.activity.ViewActivity
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
        BtnAction("自定义View相关") {
            jumpActivity(this, ViewActivity::class.java)
        },
        BtnAction("JetPack相关") {
            jumpActivity(this, JetPackActivity::class.java)
        },
        BtnAction("Database相关") {
            jumpActivity(this, DatabaseActivity::class.java)
        },
        BtnAction("Flow") {
            jumpActivity(this, FlowActivity::class.java)
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