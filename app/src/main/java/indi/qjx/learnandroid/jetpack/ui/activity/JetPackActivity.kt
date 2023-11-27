package indi.qjx.learnandroid.jetpack.ui.activity

import androidx.recyclerview.widget.LinearLayoutManager
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.common.adapter.SimpleBtnAdapter
import indi.qjx.learnandroid.common.utils.jumpActivity
import indi.qjx.learnandroid.databinding.ActivityJetpackBinding
import indi.qjx.learnandroid.model.BtnAction
import indi.qjx.libtemplate.mvvm.BaseMvvmActivity
import indi.qjx.libtemplate.mvvm.viewmodel.BaseViewModel

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/11/27
 */
class JetPackActivity : BaseMvvmActivity<BaseViewModel, ActivityJetpackBinding>() {

    private val TAG = this::class.java.simpleName

    private val functions = listOf<BtnAction>(
        BtnAction("LiveData使用") {
            jumpActivity(this, LiveDataActivity::class.java)
        },


        )
    override fun getLayoutId(): Int {
        return R.layout.activity_jetpack
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
                    this@JetPackActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            adapter = simpleBtnAdapter
        }
    }
}