package com.moviles.psychoapp.activities

import android.Manifest
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.moviles.psychoapp.R
import kotlinx.android.synthetic.main.activity_login.*
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.util.Log
import com.moviles.psychoapp.utils.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator


class LoginActivity : AppCompatActivity() {

    companion object {
        const val KEY_NAME = "psycho_app_key"
        const val TAG = "Login Activity"
    }

    val mAuth = FirebaseAuth.getInstance()
    private var cipher: Cipher? = null
    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private var cryptoObject: FingerprintManagerCompat.CryptoObject? = null
    private var fingerprintManager: FingerprintManagerCompat? = null
    private var keyguardManager: KeyguardManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupFingerprintScanner()

        lbl_no_account.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        btn_continue.setOnClickListener {
            signIn()
        }
    }

    private fun setupFingerprintScanner() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            fingerprintManager = FingerprintManagerCompat.from(this@LoginActivity)

            if (!fingerprintManager!!.isHardwareDetected) {
                Log.d(TAG, "no hardware")
                lbl_fingerprint.visibility = View.GONE
                return
            }
            if (ActivityCompat.checkSelfPermission(this@LoginActivity, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            if (fingerprintManager!!.hasEnrolledFingerprints()) {
                lbl_fingerprint.visibility = View.GONE
                Log.d(TAG, "no fingerprints")
                return
            }
            if (keyguardManager!!.isKeyguardSecure) {
                Log.d(TAG, "no lockscreen")
                lbl_fingerprint.text = getString(R.string.login_lbl_fingerprint_no_lockscreen)
                return
            }

            try {
                generateKey()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (initCypher()) {
                cryptoObject = FingerprintManagerCompat.CryptoObject(cipher)
                val helper = FingerprintHandler(this, lyt_login_constraint)
                helper.startAuth(fingerprintManager!!, cryptoObject!!)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyStore!!.load(null)
            keyGenerator!!.init(
                    KeyGenParameterSpec.Builder(KEY_NAME,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .build())

            //Generate the key//
            keyGenerator!!.generateKey()
        } catch(e: java.lang.Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun initCypher() : Boolean {
        try {
            cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
        } catch (e: Exception) {
            return false
        }
        try {
            keyStore!!.load(null)
            val key = keyStore!!.getKey(KEY_NAME, null)
            cipher!!.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun signIn() {
        if (InputValidator.validateInput(applicationContext, txt_mail, til_mail, InputsEnum.EMAIL)
                && InputValidator.validateInput(applicationContext, txt_password, til_password, InputsEnum.PASSWORD)) {
            progress_login.visibility = View.VISIBLE
            btn_continue.visibility = View.GONE
            lbl_no_account.visibility = View.GONE
            mAuth.signInWithEmailAndPassword(txt_mail.text.toString(), txt_password.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            progress_login.visibility = View.GONE
                            btn_continue.visibility = View.VISIBLE
                            lbl_no_account.visibility = View.VISIBLE
                            SnackBarCreator.showInfoSnackBar(lyt_login_constraint, getString(R.string.login_error_401), Snackbar.LENGTH_SHORT)
                        }
                    }
        }
    }

    override fun onStart() {
        if (mAuth.currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
        super.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            ResultCodes.REGISTER_SUCCESS -> {
                val mAuth = FirebaseAuth.getInstance().currentUser
                if (mAuth != null) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}
