package indi.qjx.learnandroid.ipc.ui.activity

import androidx.recyclerview.widget.LinearLayoutManager
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.common.adapter.SimpleBtnAdapter
import indi.qjx.learnandroid.databinding.ActivityIpcBinding
import indi.qjx.learnandroid.model.BtnAction
import indi.qjx.learnandroid.common.utils.jumpActivity
import indi.qjx.libtemplate.mvvm.BaseMvvmActivity
import indi.qjx.libtemplate.mvvm.viewmodel.BaseViewModel

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/11/17
 */
class IPCActivity: BaseMvvmActivity<BaseViewModel, ActivityIpcBinding>() {

    private val TAG = this::class.java.simpleName

    private val functions = listOf<BtnAction>(
        BtnAction("创建新进程") {
            jumpActivity(this, IPCSecondActivity::class.java)
        },
    )
    override fun getLayoutId(): Int {
       return R.layout.activity_ipc
    }

    override fun bindViewModel() {

    }

    override fun init() {
        title =  this::class.java.simpleName
        val simpleBtnAdapter = SimpleBtnAdapter()
        simpleBtnAdapter.setNewData(functions)
        mViewDataBind.recyclerview.apply {
            layoutManager =
                LinearLayoutManager(
                    this@IPCActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            adapter = simpleBtnAdapter
        }
    }
}