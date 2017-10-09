package com.moviles.psychoapp.world

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Created by juanacevedo on 10/8/17.
 */

@IgnoreExtraProperties
data class Exam(val company: String = "Company Name loading...",
                val description: String = "Exam description loading...",
                val title: String = "Exam Title", val user: String = "user id loading...",
                val username: String = "username loading...",
                val tests: Map<String, Test> = HashMap<String, Test>()) {
    val id: String = "id loading..."
}