package com.moviles.psychoapp.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.moviles.psychoapp.ExamResultsActivity
import com.moviles.psychoapp.R
import com.moviles.psychoapp.fragments.TestDetailsFragment
import com.moviles.psychoapp.utils.RequestCodes
import com.moviles.psychoapp.utils.ResultCodes
import com.moviles.psychoapp.world.BriefExam
import com.moviles.psychoapp.world.Exam
import com.moviles.psychoapp.world.Test
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import java.text.SimpleDateFormat
import java.util.*

class TestManagerActivity : AppCompatActivity(), TestDetailsFragment.OnTestDetailsFragmentListener {
    private val examId by lazy { intent.getStringExtra("examId") }
    private var testIndex = 0
    private lateinit var exam: Exam
    private val tests: ArrayList<Test> by lazy { ArrayList<Test>() }
    private var actualTest = 0
    private var examScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_manager)

        processTests()
    }

    private fun processTests() {
        if (testIndex == 0) {
            var started = false
            FirebaseDatabase.getInstance().getReference("exams")
                    .child(examId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(ds: DatabaseError) {}
                override fun onDataChange(ds1: DataSnapshot) {
                    if (ds1.exists()) {
                        exam = ds1.getValue(Exam::class.java)!!
                        for (i in exam.tests.keys) {
                            FirebaseDatabase.getInstance().getReference("tests")
                                    .child(i).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(ds: DatabaseError) {}
                                override fun onDataChange(ds: DataSnapshot) {
                                    if (ds.exists()) {
                                        val aTest = ds.getValue(Test::class.java)!!
                                        aTest.id = ds.key
                                        aTest.time = exam.tests.get(i)!!.time
                                        aTest.weight = exam.tests.get(i)!!.weight
                                        aTest.maxValue = exam.tests.get(i)!!.maxValue
                                        tests.add(aTest)
                                        if (!started) {
                                            started = true
                                            displayTest(0)
                                        }
                                    }
                                }
                            })
                        }
                    }
                }

            })
            testIndex++
        } else {
            if (tests.size > testIndex) {

            } else {
                alert("Exam finished !") {
                    positiveButton("Continue", {
                        val date = SimpleDateFormat("dd/MM/yyyy").format(Date())
                        val bExam = BriefExam(date = date, name = exam.title, employer = exam.user, maxScore = 100.0, score = examScore.toDouble())
                        bExam.id = examId
                        startActivity<ExamResultsActivity>("exam" to Gson().toJson(bExam))
                    })
                }.show()
            }
        }
    }

    private fun displayTest(index: Int) {
        val aTest = tests[index]
        val testFragment = TestDetailsFragment.newInstance(aTest.id, aTest.time, aTest.weight, aTest.maxValue)
        supportFragmentManager.beginTransaction()
                .replace(R.id.test_manager_fragment, testFragment)
                .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCodes.TEST_TRY -> {
                if (resultCode == ResultCodes.TEST_ACCELEROMETER_FINISH) {
                    examScore += (data!!.getLongExtra("score", 0).toDouble() * data.getFloatExtra("weight", 0F).toDouble()).toInt()
                    processTests()
                }
            }
        }
    }

    //FRAGMENT LISTENER
    override fun onFragmentInteraction(action: String) {
        when (action) {
            ACCELEROMETER -> {
                val intent = Intent(this, AccelerometerActivity::class.java)
                intent.putExtra("time", exam.tests.get(tests[actualTest].id)!!.time)
                intent.putExtra("maxValue", exam.tests.get(tests[actualTest].id)!!.maxValue)
                intent.putExtra("weight", exam.tests.get(tests[actualTest].id)!!.weight)
                startActivityForResult(intent, RequestCodes.TEST_TRY)
            }
        }
    }

    companion object {
        const val ACCELEROMETER = "ACCELEROMETER"
    }
}
