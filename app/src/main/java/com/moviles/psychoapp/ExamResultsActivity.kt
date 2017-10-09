package com.moviles.psychoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.moviles.psychoapp.activities.MainActivity
import com.moviles.psychoapp.world.BriefExam
import kotlinx.android.synthetic.main.activity_exam_results.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity

class ExamResultsActivity : AppCompatActivity() {

    private val bExam: BriefExam by lazy { Gson().fromJson(intent.getStringExtra("exam"), BriefExam::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam_results)

        exam_results_date.text = bExam.date
        exam_results_name.text = bExam.name
        exam_results_score.text = "${bExam.score} out of ${bExam.maxScore}"
        exam_results_btn_finish.setOnClickListener {
            alert("Finishing") {
                positiveButton("Continue", {
                    FirebaseDatabase.getInstance().getReference("candidates").child(FirebaseAuth.getInstance().uid)
                            .child("exams").child(bExam.id).setValue(bExam).addOnCompleteListener {
                        if (it.isSuccessful) {
                            startActivity<MainActivity>()
                            finish()
                        }
                    }
                })
            }.show()
        }
    }
}
