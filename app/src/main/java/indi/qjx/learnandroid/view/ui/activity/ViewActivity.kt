package indi.qjx.learnandroid.view.ui.activity

import androidx.recyclerview.widget.LinearLayoutManager
import indi.qjx.base.mvvm.BaseMvvmActivity
import indi.qjx.base.mvvm.viewmodel.BaseViewModel
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.common.adapter.SimpleBtnAdapter
import indi.qjx.learnandroid.common.utils.jumpActivity
import indi.qjx.learnandroid.databinding.ActivityViewBinding
import indi.qjx.learnandroid.ipc.ui.activity.*
import indi.qjx.learnandroid.model.BtnAction

class ViewActivity : BaseMvvmActivity<BaseViewModel, ActivityViewBinding>() {

    private val TAG = this::class.java.simpleName

    private val functions = listOf<BtnAction>(
        BtnAction("View基础") {
            jumpActivity(this, ViewSimpleActivity::class.java)
        },
        BtnAction("滑动冲突外部拦截") {
            jumpActivity(this, ConflictOutsideActivity::class.java)
        },
        BtnAction("滑动冲突内部拦截") {
            jumpActivity(this, ConflictInsideActivity::class.java)
        },

    )
    override fun getLayoutId(): Int {
        return R.layout.activity_view
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
                    this@ViewActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            adapter = simpleBtnAdapter
        }
    }
}