package indi.qjx.learnandroid.flow.ui.activity

import androidx.lifecycle.lifecycleScope
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.databinding.ActivityFlowBinding
import indi.qjx.learnandroid.flow.viewmodel.FlowVM
import indi.qjx.libtemplate.mvvm.BaseMvvmActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/12/04
 */
class FlowActivity : BaseMvvmActivity<FlowVM, ActivityFlowBinding>() {

    private val TAG = this::class.java.simpleName

    override fun getLayoutId(): Int {
        return R.layout.activity_flow
    }

    override fun bindViewModel() {
        mViewDataBind.button.setOnClickListener {
            lifecycleScope.launch {
                mViewModel.timeFlow.collect {
                    mViewDataBind.textView.text = it.toString()
                }
            }
        }

    }

    override fun init() {
        title =  this::class.java.simpleName
    }
}