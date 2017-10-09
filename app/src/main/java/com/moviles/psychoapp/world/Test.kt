package com.moviles.psychoapp.world

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Created by juanacevedo on 10/8/17.
 */

@IgnoreExtraProperties
data class Test(val name: String = "Test Name loading...",
                val description: String = "Test Description loading...",
                val privateId: String = "private id loading...",
                val instructions: String = "Test instructions loading...",
                var maxValue: Long = 100L,
                var time: Long = 60L,
                var weight: Float = 100F) {
    var id: String = "id Loading..."
}