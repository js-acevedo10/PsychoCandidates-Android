package com.moviles.psychoapp.utils

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View

/**
 * Created by juanacevedo on 9/21/17.
 */

class SnackBarCreator {
    companion object {
        fun showInfoSnackBar(view: View, message: String, duration: Int) {
            Snackbar.make(view, message, duration).show()
        }
    }
}