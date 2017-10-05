package com.moviles.psychoapp.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.moviles.psychoapp.R
import kotlinx.android.synthetic.main.activity_login.*
import android.content.pm.PackageManager
import com.moviles.psychoapp.utils.*


class LoginActivity : AppCompatActivity() {

    companion object {
        const val KEY_NAME = "psycho_app_key"
        const val TAG = "LoginActivity"
    }

    val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        lbl_no_account.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        btn_continue.setOnClickListener {
            signIn()
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
