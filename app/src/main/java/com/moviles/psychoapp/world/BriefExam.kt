package com.moviles.psychoapp.world

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Class that represents a brief part of an exam for RecyclerViews.
 */

@IgnoreExtraProperties
data class BriefExam(val date: String = "Test Date loading...", val employer: String = "Test employer loading...", val maxScore: Double = 100.0, val name: String = "Exam name loading...", val score: Double = 0.0) {
    var id: String = "id loading..."
}