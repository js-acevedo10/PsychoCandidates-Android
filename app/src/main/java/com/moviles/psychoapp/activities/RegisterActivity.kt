package com.moviles.psychoapp.activities

import android.app.DatePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.moviles.psychoapp.R
import com.moviles.psychoapp.utils.InputValidator
import com.moviles.psychoapp.utils.InputsEnum
import com.moviles.psychoapp.utils.ResultCodes
import com.moviles.psychoapp.world.Candidate
import kotlinx.android.synthetic.main.activity_register.*
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Register Activity"
    }

    val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        txt_birth.setOnClickListener {
            DatePickerDialog(this@RegisterActivity,
                    object : DatePickerDialog.OnDateSetListener {
                        override fun onDateSet(p0: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, monthOfYear)
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            updateBirthLabel()
                        }

                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btn_continue.setOnClickListener {
            if (InputValidator.validateInput(applicationContext, txt_name, til_name, InputsEnum.NAME)
                    && InputValidator.validateInput(applicationContext, txt_birth, til_birth, InputsEnum.BIRTHDAY)
                    && InputValidator.validateInput(applicationContext, txt_mail, til_mail, InputsEnum.EMAIL)
                    && InputValidator.validateInput(applicationContext, txt_password, til_password, InputsEnum.PASSWORD)) {
                createUser()
            }
        }
    }

    private fun createUser() {
        btn_continue.visibility = View.GONE
        progress_register.visibility = View.VISIBLE
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(txt_mail.text.toString(), txt_password.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val candidatesRef = FirebaseDatabase.getInstance().getReference("candidates")
                        candidatesRef.child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(
                                Candidate(name = txt_name.text.toString(), birthDay = txt_birth.text.toString(), email = txt_mail.text.toString()))
                                .addOnCompleteListener { task2 ->
                                    if (task2.isSuccessful) {
                                        setResult(ResultCodes.REGISTER_SUCCESS)
                                        finish()
                                    } else {
                                        btn_continue.visibility = View.VISIBLE
                                        progress_register.visibility = View.GONE
                                    }
                                }
                    } else {
                        btn_continue.visibility = View.VISIBLE
                        progress_register.visibility = View.GONE
                    }
                }
    }

    private fun updateBirthLabel() {
        val format = "dd/MM/yyyy"
        val df = SimpleDateFormat(format, Locale.getDefault())
        txt_birth.setText(df.format(calendar.time))
    }
}
