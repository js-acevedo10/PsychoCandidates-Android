package com.moviles.psychoapp.utils

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.widget.TextView
import com.moviles.psychoapp.R

/**
 * Created by juanacevedo on 9/21/17.
 */

class InputValidator {
    companion object {
        fun validateInput(context: Context, txt: TextView, til: TextInputLayout, type: InputsEnum) : Boolean {
            val text = txt.text.toString()
            when (type) {
                InputsEnum.NAME -> {
                    if (txt.text.isBlank()) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_blank, "name")
                        return false
                    } else if (text.length > 30) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_long, "name")
                        return false
                    } else if (text.length < 8) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_short, "name")
                        return false
                    } else if (text.contains("@") || text.contains("#") || text.contains("$") || text.contains("?")) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_invalid, "name")
                        return false
                    } else {
                        til.isErrorEnabled = false
                        til.error = null
                        return true
                    }
                }
                InputsEnum.BIRTHDAY -> {
                    if (txt.text.isBlank()) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_blank, "date")
                        return false
                    } else if (text.length > 10) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_long, "date")
                        return false
                    } else if (text.length < 10) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_short, "date")
                        return false
                    } else if (!text.contains("/")) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_invalid, "date")
                        return false
                    } else {
                        til.isErrorEnabled = false
                        til.error = null
                        return true
                    }
                }
                InputsEnum.EMAIL -> {
                    if (txt.text.isBlank()) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_blank, "email")
                        return false
                    } else if (text.length > 50) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_long, "email")
                        return false
                    } else if (!text.contains("@") || !text.contains(".") || text.contains(" ")) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_invalid, "email")
                        return false
                    } else {
                        til.isErrorEnabled = false
                        til.error = null
                        return true
                    }
                }
                InputsEnum.PASSWORD -> {
                    if (txt.text.isBlank()) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_blank, "password")
                        return false
                    } else if (text.length > 30) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_long_l, "password", "It can't have more than 30 characters.")
                        return false
                    } else if (text.length < 5) {
                        til.isErrorEnabled = true
                        til.error = context.getString(R.string.register_error_short_l, "password", "It can't have less than 5 characters.")
                        return false
                    } else {
                        til.isErrorEnabled = false
                        til.error = null
                        return true
                    }
                }
            }
        }
    }
}