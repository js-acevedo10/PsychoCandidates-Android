package com.moviles.psychoapp.world

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Class that represents a brief part of an exam for RecyclerViews.
 */

@IgnoreExtraProperties
data class BriefExam(val date : String = "N/A", val employer : String = "N/A", val maxScore : Double = 100.0, val name : String = "Exam", val score : Double = 0.0, val time : Double = 0.0) {
    var id: String = ""
}