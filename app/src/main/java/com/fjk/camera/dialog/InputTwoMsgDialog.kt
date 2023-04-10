package com.fjk.camera.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.fjk.camera.R
import com.fjk.camera.databinding.DialogAddNetCameraBinding

class InputTwoMsgDialog : Dialog {

    private var _binding: DialogAddNetCameraBinding? = null
    val binding get() = _binding!!

    constructor(context: Context) : super(context, R.style.CustomDialogNoDim) {
        initView()
    }

    private fun initView() {
        _binding = DialogAddNetCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        isCanceledOnTouchOutside = false
        binding.cardView.setBackgroundColor(Color.WHITE)
    }

    fun showDialog(target: View) {
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
        var titleContent: String? = null
        var item1Name: String? = null
        var item1Input: String? = null
        var item1Hint: String? = null
        var item2Name: String? = null
        var item2Hint: String? = null
        var item2Input: String? = null
        var onConfirmListener: OnConfirmListener? = null
        var onCancelListener: OnCancelListener? = null

        fun setTitle(title: String): Builder {
            this.titleContent = title
            return this
        }

        fun setItem1Name(item1Name: String): Builder {
            this.item1Name = item1Name
            return this
        }

        fun setItem1Input(item1Input: String): Builder {
            this.item1Input = item1Input
            return this
        }

        fun setItem1Hint(item1Hint: String): Builder {
            this.item1Hint = item1Hint
            return this
        }

        fun setItem2Name(item2Name: String): Builder {
            this.item2Name = item2Name
            return this
        }

        fun setItem2Input(item2Input: String): Builder {
            this.item2Input = item2Input
            return this
        }

        fun setItem2Hint(item2Hint: String): Builder {
            this.item2Hint = item2Hint
            return this
        }

        fun setOnConfirmListener(confirmListener: OnConfirmListener): Builder {
            this.onConfirmListener = confirmListener
            return this
        }

        fun setOnCancelListener(cancelListener: OnCancelListener): Builder {
            this.onCancelListener = cancelListener
            return this
        }

        fun onCreate(): InputTwoMsgDialog {
            val dialog = InputTwoMsgDialog(context)
            with(dialog.binding) {
                //设置参数
                if (titleContent != null) this.title.text = titleContent
                if (item1Name != null) this.txtItem1Name.text = item1Name
                if (item1Input != null) this.edItem1Input.setText(item1Input.toString())
                if (item1Hint != null) this.edItem1Input.hint = item1Hint
                if (item2Name != null) this.txtItem2Name.text = item2Name
                if (item2Input != null) this.edItem2Input.setText(item2Input.toString())
                if (item2Hint != null) this.edItem2Input.hint = item2Hint
                if (onConfirmListener != null) {
                    this.btnConfirm.setOnClickListener { onConfirmListener!!.onClick(dialog) }
                }
                if (onCancelListener != null) {
                    this.btnCancel.setOnClickListener { onCancelListener!!.onClick(dialog) }
                }
                //输入1获取焦点
                this.edItem1Input.isFocusable = true
                this.edItem1Input.isFocusableInTouchMode = true
                this.edItem1Input.requestFocus()

                //确认按钮的状态
                if (item2Input == null || item2Input.equals("")) {
                    dialog.binding.btnConfirm.isEnabled = false
                }
                this.imgClose.setOnClickListener {
                    dialog.dismiss()
                }

                this.btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                this.edItem2Input.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(p0: Editable?) {
                        dialog.binding.btnConfirm.isEnabled =
                            p0.toString().isNotEmpty()
                    }

                })
                this.imgClose
            }
            return dialog
        }
    }

    // 点击弹窗取消按钮回调
    interface OnCancelListener {
        fun onClick(dialog: InputTwoMsgDialog)
    }

    // 点击弹窗跳转回调
    interface OnConfirmListener {
        fun onClick(dialog: InputTwoMsgDialog)
    }
}