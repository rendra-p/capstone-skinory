package com.capstone.skinory.ui.customview

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ChangePasswordInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputLayout(context, attrs, defStyleAttr) {

    private val editText: TextInputEditText

    init {
        editText = TextInputEditText(context)

        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        editText.layoutParams = layoutParams

        addView(editText)

        editText.inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun getPassword(): String {
        return editText.text.toString()
    }

    fun isPasswordValid(): Boolean {
        return validatePassword(editText.text.toString())
    }

    private fun validatePassword(password: String): Boolean {
        return when {
            password.isNotEmpty() && !password.first().isUpperCase() -> {
                isErrorEnabled = true
                error = "Password must begin with an uppercase letter"
                false
            }
            password.isNotEmpty() && password.length < 8 -> {
                isErrorEnabled = true
                error = "Password must be at least 8 characters"
                false
            }
            else -> {
                isErrorEnabled = false
                error = null
                true
            }
        }
    }
}