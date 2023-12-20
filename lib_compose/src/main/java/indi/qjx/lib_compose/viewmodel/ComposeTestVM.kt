package indi.qjx.lib_compose.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/12/20
 */
class ComposeTestVM : ViewModel() {
    //使用liveData
/*    private val _count = MutableLiveData<Int>()
    private val _doubleCount = MutableLiveData<Int>()

    val count: LiveData<Int> = _count
    val doubleCount: LiveData<Int> = _doubleCount*/

    //使用Flow
    private val _count = MutableStateFlow(0)
    private val _doubleCount = MutableStateFlow(0)

    val count = _count.asStateFlow()
    val doubleCount = _doubleCount.asStateFlow()

    fun incrementCount() {
        _count.value = (_count.value ?: 0).plus(1)
    }

    fun incrementDoubleCount() {
        _doubleCount.value = (_doubleCount.value ?: 0).plus(2)
    }
}