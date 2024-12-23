package com.capstone.skinory.ui.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout

class PasswordInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputLayout(context, attrs, defStyleAttr) {

    init {
        post {
            editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val password = s?.toString() ?: ""
                    when {
                        password.isNotEmpty() && !password.first().isUpperCase() -> {
                            isErrorEnabled = true
                            error = "Password must begin with an uppercase letter"
                        }
                        password.isNotEmpty() && password.length < 8 -> {
                            isErrorEnabled = true
                            error = "Password must be at least 8 characters"
                        }
                        else -> {
                            isErrorEnabled = false
                            error = null
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }
}