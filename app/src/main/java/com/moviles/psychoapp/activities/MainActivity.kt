package com.moviles.psychoapp.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.moviles.psychoapp.R
import com.moviles.psychoapp.adapters.MyExamsAdapter
import com.moviles.psychoapp.world.BriefExam
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val mAuth = FirebaseAuth.getInstance()
    val myExams = ArrayList<BriefExam>()
    var myExamsAdapter: MyExamsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                firebaseAuth.signOut()
            }
        }

        getMyExams()
    }

    override fun onStart() {
        mAuth.currentUser!!.reload()
        if (mAuth.currentUser == null) {
            goToLogin()
        }
        checkUserIsStillLogged()
        super.onStart()
    }

    private fun getMyExams() {
        FirebaseDatabase.getInstance().getReference("candidates")
                .child(mAuth.currentUser!!.uid).child("exams").addChildEventListener(
                object : ChildEventListener {
                    override fun onChildAdded(ds: DataSnapshot, p1: String?) {
                        var isUnique = true
                        myExams.forEach {
                            if (it.id == ds.key) {
                                isUnique = false
                            }
                        }
                        if (isUnique) {
                            myExams.add(ds.getValue(BriefExam::class.java)!!)
                            if (myExamsAdapter == null) {
                                myExamsAdapter = MyExamsAdapter(myExams, R.layout.item_my_exams)
                                recycler_my_exams.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayout.VERTICAL, false)
                                recycler_my_exams.adapter = myExamsAdapter
                            } else {
                                myExamsAdapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}
                    override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}
                    override fun onChildRemoved(p0: DataSnapshot?) {}
                }
        )
    }

    private fun checkUserIsStillLogged() {
        FirebaseDatabase.getInstance().getReference("candidates")
                .child(mAuth.currentUser!!.uid).addValueEventListener(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if (!p0!!.exists()) {
                            mAuth.signOut()
                            goToLogin()
                        }
                    }
                }
        )
    }

    private fun goToLogin() {
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }
}
