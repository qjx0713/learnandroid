package indi.qjx.learnandroid.database.ui.activity

import androidx.recyclerview.widget.LinearLayoutManager
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.common.adapter.SimpleBtnAdapter
import indi.qjx.learnandroid.common.utils.jumpActivity
import indi.qjx.learnandroid.databinding.ActivityDatabaseBinding
import indi.qjx.learnandroid.model.BtnAction
import indi.qjx.base.mvvm.BaseMvvmActivity
import indi.qjx.base.mvvm.viewmodel.BaseViewModel

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/12/01
 */
class DatabaseActivity : BaseMvvmActivity<BaseViewModel, ActivityDatabaseBinding>() {

    private val TAG = this::class.java.simpleName

    private val functions = listOf<BtnAction>(
        BtnAction("SQLite数据库") {
            jumpActivity(this, SQLiteActivity::class.java)
        },
        BtnAction("Room数据库") {
            jumpActivity(this, RoomActivity::class.java)
        },


        )
    override fun getLayoutId(): Int {
        return R.layout.activity_database
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
                    this@DatabaseActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            adapter = simpleBtnAdapter
        }
    }
}