package indi.qjx.learnandroid.adapter

import android.widget.Button
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import indi.qjx.learnandroid.R
import indi.qjx.learnandroid.model.BtnAction

/**
 *     author : qjx
 *     e-mail : qianjx1@chinatelecom.cn
 *     time   : 2023/11/16
 */
class SimpleBtnAdapter : BaseQuickAdapter<BtnAction, BaseViewHolder>(R.layout.item_simple_button) {
    override fun convert(helper: BaseViewHolder?, item: BtnAction?) {
        helper?.apply {
            setText(R.id.btn, item?.text)
            itemView.setOnClickListener {
                item?.callback?.invoke()
            }
        }
    }
}