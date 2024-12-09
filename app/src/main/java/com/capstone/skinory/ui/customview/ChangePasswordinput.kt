package com.capstone.skinory.ui.customview

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import com.capstone.skinory.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ChangePasswordinput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputLayout(context, attrs, defStyleAttr) {

    private val editText: TextInputEditText

    init {
        // Programmatically create the EditText
        editText = TextInputEditText(context)

        // Set layout parameters
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        editText.layoutParams = layoutParams

        // Add the EditText to this TextInputLayout
        addView(editText)

        // Set input type to password
        editText.inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Add TextWatcher for validation
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Mendapatkan password dari EditText
    fun getPassword(): String {
        return editText.text.toString()
    }

    // Memeriksa validitas password
    fun isPasswordValid(): Boolean {
        return validatePassword(editText.text.toString())
    }

    // Validasi password
    private fun validatePassword(password: String): Boolean {
        return when {
            password.isNotEmpty() && !password.first().isUpperCase() -> {
                isErrorEnabled = true
                error = "Password harus diawali huruf besar"
                false
            }
            password.isNotEmpty() && password.length < 8 -> {
                isErrorEnabled = true
                error = "Password minimal 8 karakter"
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