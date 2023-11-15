package indi.qjx.libtemplate.mvvm.impl

/**
 * 进入页面加载框
 */
interface ILoadingDialog {
    fun loadingMsg(): String
    fun showLoading()
    fun hideLoading()
}