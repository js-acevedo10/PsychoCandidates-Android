package com.moviles.psychoapp.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.moviles.psychoapp.R
import com.moviles.psychoapp.world.Exam
import com.moviles.psychoapp.world.Test
import kotlinx.android.synthetic.main.activity_exam_details.*
import org.jetbrains.anko.startActivity
import java.util.*

class ExamDetailsActivity : AppCompatActivity() {

    private val examId by lazy { intent.getStringExtra("examId") }
    private lateinit var exam: Exam
    private lateinit var tests: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam_details)


        FirebaseDatabase.getInstance().getReference("exams")
                .child(examId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onDataChange(ds: DataSnapshot) {
                        if (ds.exists()) {
                            exam = ds.getValue(Exam::class.java)!!
                            processExamData()
                        }
                    }
                }
        )

        exam_details_btn_take.setOnClickListener {
            startActivity<TestManagerActivity>("examId" to examId)
            finish()
        }
    }

    private fun processExamData() {
        exam_details_company.text = exam.company
        exam_details_title.text = exam.title
        exam_details_description.text = exam.description
        tests = arrayListOf()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tests)
        exam_details_list_tests.adapter = adapter
        for (i in exam.tests.keys) {
            FirebaseDatabase.getInstance().getReference("tests")
                    .child(i).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(ds: DatabaseError) {}
                override fun onDataChange(ds: DataSnapshot) {
                    if (ds.exists()) {
                        val test = ds.getValue(Test::class.java) as Test
                        tests.add(test.name)
                        adapter.notifyDataSetChanged()
                    }
                }
            })
        }
    }
}
