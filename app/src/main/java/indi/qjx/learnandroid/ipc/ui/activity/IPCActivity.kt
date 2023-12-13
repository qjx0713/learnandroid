package indi.qjx.learnandroid.ipc.ui.activity

import androidx.recyclerview.widget.LinearLayoutManager
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.common.adapter.SimpleBtnAdapter
import indi.qjx.learnandroid.databinding.ActivityIpcBinding
import indi.qjx.learnandroid.model.BtnAction
import indi.qjx.learnandroid.common.utils.jumpActivity
import indi.qjx.base.mvvm.BaseMvvmActivity
import indi.qjx.base.mvvm.viewmodel.BaseViewModel

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
        BtnAction("使用Messenger 跨进程通信") {
            jumpActivity(this, MessengerActivity::class.java)
        },
        BtnAction("使用AIDL 跨进程通信") {
            jumpActivity(this, BookManagerActivity::class.java)
        },
        BtnAction("使用ContentProvider 跨进程通信") {
            jumpActivity(this, ProviderActivity::class.java)
        },
        BtnAction("使用Socket 跨进程通信") {
            jumpActivity(this, TCPClientActivity::class.java)
        },
        BtnAction("使用BinderPool 跨进程通信") {
            jumpActivity(this, BinderPoolActivity::class.java)
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