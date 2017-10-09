package com.moviles.psychoapp

import android.app.Application
import android.os.StrictMode

/**
 * Created by juanacevedo on 10/6/17.
 */

class PsychoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

}