package com.fjk.camera.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.PopupMenu
import com.fjk.camera.R
import com.fjk.camera.databinding.DialogWarnBinding


class DeleteDialog : Dialog {

    private var _binding: DialogWarnBinding? = null
    private val binding get() = _binding!!

    constructor(context: Context) : super(context, R.style.CustomDialogNoDim) {
        initView()
    }

    private fun initView() {
        _binding = DialogWarnBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        this.isCanceledOnTouchOutside = false
        binding.cardView.setBackgroundColor(Color.WHITE)
    }

    fun showDialog(target:View){
//        if (isShowing) {
//            return
//        }
//        showAt(
//            target,
//            LocationEnum.RIGHT_OF,
//            LocationEnum.ALIGN_TOP,
//            context.resources.getDimensionPixelSize(R.dimen.s_dp_16),
//            0
//        )
    }

    class Builder(val context: Context) {
        private var titleContent: String? = null
        var deleteContent: String? = null
        var onDeleteListener: OnDeleteListener? = null
        private var onCancelListener: OnCancelListener? = null

        fun setTitle(title: String): Builder {
            this.titleContent = title
            return this
        }

        fun setDeleteContent(contentInfo: String): Builder {
            this.deleteContent = contentInfo
            return this
        }

        fun setDeleteListener(deleteListener: OnDeleteListener): Builder {
            this.onDeleteListener = deleteListener
            return this
        }

        fun setOnCancelListener(cancelListener: OnCancelListener): Builder {
            this.onCancelListener = cancelListener
            return this
        }

        fun onCreate(): DeleteDialog {
            val dialog = DeleteDialog(context)
            with(dialog.binding) {
                //设置参数
                if (titleContent != null) this.titleDelete.text = titleContent
                if (deleteContent != null) this.tvContentDelete.text = deleteContent
                if (onDeleteListener != null) {
                    this.btnDelete.setOnClickListener { onDeleteListener!!.onClick(dialog) }
                }
                if (onCancelListener != null) {
                    this.btnCancel.setOnClickListener { onCancelListener!!.onClick(dialog) }
                }
                this.ivClose.setOnClickListener{
                    dialog.dismiss()
                }
            }
            return dialog
        }
    }

    // 点击弹窗取消按钮回调
    interface OnCancelListener {
        fun onClick(dialog: DeleteDialog)
    }

    // 点击弹窗跳转回调
    interface OnDeleteListener {
        fun onClick(dialog: DeleteDialog)
    }
}