package indi.qjx.learnandroid.view.ui.activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import indi.qjx.base.mvvm.BaseMvvmActivity
import indi.qjx.base.mvvm.viewmodel.BaseViewModel
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.databinding.ActivityViewAnimBinding
import java.util.*

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/12/14
 */
class ViewAnimActivity: BaseMvvmActivity<BaseViewModel, ActivityViewAnimBinding>() {

    companion object{
        @JvmStatic
        val TAG: String = this.javaClass.simpleName
    }
    override fun getLayoutId(): Int {
        return R.layout.activity_view_anim
    }

    override fun bindViewModel() {
        mViewDataBind.btnStartAnim.setOnClickListener {
//            val animation = AnimationUtils.loadAnimation(this, R.anim.animation_test)
//            mViewDataBind.btnTest.startAnimation(animation)


            val anim = ObjectAnimator.ofFloat(mViewDataBind.btnTest, "rotation", 0f, 270f)
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    val view = mViewDataBind.btnTest
                    Log.d(TAG, "onAnimationStart:width=${view.width},height=${view.height}, x=${view.x},y=${view.y},left = ${view.left}, top= ${view.top}, translationx=${view.translationX},translationy=${view.translationY}")
                }

                override fun onAnimationEnd(animation: Animator?) {
                    val view = mViewDataBind.btnTest
                    Log.d(TAG, "onAnimationStart:width=${view.width},height=${view.height}, x=${view.x},y=${view.y},left = ${view.left}, top= ${view.top}, translationx=${view.translationX},translationy=${view.translationY}")

                    val srcWidth = mViewDataBind.btnTest.width
                    val srcHeight = mViewDataBind.btnTest.height
                    //旋转结束后重设宽高
                    val layoutParam = mViewDataBind.btnTest.layoutParams
                    layoutParam.height = srcWidth
                    layoutParam.width = srcHeight
                    mViewDataBind.btnTest.layoutParams = layoutParam
                    Log.d(TAG, "onAnimationStart:width=${view.width},height=${view.height}, x=${view.x},y=${view.y},left = ${view.left}, top= ${view.top}, translationx=${view.translationX},translationy=${view.translationY}")

                    mViewDataBind.btnTest.translationX = (-(srcHeight - srcWidth) / 2).toFloat()
                    mViewDataBind.btnTest.translationY = (-(srcWidth - srcHeight) / 2).toFloat()
                    Log.d(TAG, "onAnimationStart:width=${view.width},height=${view.height}, x=${view.x},y=${view.y},left = ${view.left}, top= ${view.top}, translationx=${view.translationX},translationy=${view.translationY}")

                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
            anim.start()

        }

    }

    override fun init() {
        createList()
    }

    fun createList() {
        val listView = mViewDataBind.list
        val datas = ArrayList<String>()
        for (i in 0..49) {
            datas.add("name $i")
        }
        val adapter = ArrayAdapter(
            this,
            R.layout.content_list_item, R.id.name, datas
        )
        listView.adapter = adapter
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                Toast.makeText(
                    this, "click item",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}