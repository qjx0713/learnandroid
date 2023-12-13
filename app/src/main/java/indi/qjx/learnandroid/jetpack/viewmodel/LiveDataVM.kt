package indi.qjx.learnandroid.jetpack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import indi.qjx.base.mvvm.viewmodel.BaseViewModel

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/11/27
 */
class LiveDataVM : BaseViewModel() {
    val _data = MutableLiveData<Int>()

    init {
        _data.value = 0
    }

    val data: LiveData<Int>
        get() = _data

    fun add() {
        _data.value = _data.value?.plus(1)
    }

    fun reNotify() {
        _data.value = _data.value
    }
}