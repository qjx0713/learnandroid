package indi.qjx.learnandroid.database.ui.activity

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.common.adapter.SimpleBtnAdapter
import indi.qjx.learnandroid.database.dao.UserDao
import indi.qjx.learnandroid.database.database.AppDatabase
import indi.qjx.learnandroid.database.entity.User
import indi.qjx.learnandroid.databinding.ActivityRoomBinding
import indi.qjx.learnandroid.model.BtnAction
import indi.qjx.base.mvvm.BaseMvvmActivity
import indi.qjx.base.mvvm.viewmodel.BaseViewModel
import kotlin.concurrent.thread

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/11/30
 */
class RoomActivity  : BaseMvvmActivity<BaseViewModel, ActivityRoomBinding>() {

    private val TAG = this::class.java.simpleName
    lateinit var userDao :UserDao
    val user1 = User("Tom", "Brady", 40)
    val user2 = User("Tom", "Hanks", 63)

    private val functions = listOf<BtnAction>(
        BtnAction("添加数据") {
            addData()
        },
        BtnAction("更新数据") {
            updateData()
        },
        BtnAction("删除数据") {
            deleteData()
        },
        BtnAction("查询数据") {
            queryData()
        },


        )

    private fun queryData() {
        thread {
            for (user in userDao.loadAllUsers()) {
                Log.d(TAG, user.toString())
            }
        }
    }

    private fun deleteData() {
        thread {
            userDao.deleteUserByLastName("Hanks")
        }
    }

    private fun updateData() {
        thread {
            user1.age = 42
            userDao.updateUser(user1)
        }
    }

    private fun addData() {
        thread {
            user1.id = userDao.insertUser(user1)
            user2.id = userDao.insertUser(user2)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_room
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
                    this@RoomActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            adapter = simpleBtnAdapter
        }
        userDao = AppDatabase.getDatabase(this).userDao()
    }
}