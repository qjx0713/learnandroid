package indi.qjx.learnandroid.flow.viewmodel

import indi.qjx.libtemplate.mvvm.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/12/04
 */
class FlowVM : BaseViewModel() {
    val timeFlow = flow {
        var time = 0
        while (true) {
            emit(time)
            delay(1000)
            time++
        }
    }

}