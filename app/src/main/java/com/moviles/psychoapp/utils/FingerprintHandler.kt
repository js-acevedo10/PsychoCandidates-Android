package com.moviles.psychoapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import android.view.View
import com.moviles.psychoapp.activities.MainActivity

/**
 * Created by juanacevedo on 9/22/17.
 */

@RequiresApi(Build.VERSION_CODES.M)
class FingerprintHandler (val context: Context, val view: View) : FingerprintManagerCompat.AuthenticationCallback() {

    val cancellationSignal = CancellationSignal()

    fun startAuth(manager: FingerprintManagerCompat, cryptoObject: FingerprintManagerCompat.CryptoObject) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        manager.authenticate(cryptoObject, 0, cancellationSignal, this@FingerprintHandler, null)
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        SnackBarCreator.showInfoSnackBar(view, "Authentication error.", Snackbar.LENGTH_SHORT)
        (context as MainActivity).showFingerprintError()
    }

    override fun onAuthenticationFailed() {
        SnackBarCreator.showInfoSnackBar(view, "Authentication failed.", Snackbar.LENGTH_SHORT)
        (context as MainActivity).showFingerprintError()
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        SnackBarCreator.showInfoSnackBar(view, "Authentication error.", Snackbar.LENGTH_SHORT)
        (context as MainActivity).showFingerprintError()
    }

    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
//        SnackBarCreator.showInfoSnackBar(view, "Authentication success.", Snackbar.LENGTH_SHORT)
        (context as MainActivity).hideFingerprintDialog()
    }
}