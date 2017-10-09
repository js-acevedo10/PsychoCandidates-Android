package com.moviles.psychoapp.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.moviles.psychoapp.R
import com.moviles.psychoapp.utils.ResultCodes
import kotlinx.android.synthetic.main.activity_accelerometer.*
import org.jetbrains.anko.alert

class AccelerometerActivity : AppCompatActivity(), SensorEventListener {

    private val time by lazy { intent.getLongExtra("time", 60L) }
    private val maxValue by lazy { intent.getLongExtra("maxValue", 100L) }
    private val weight by lazy { intent.getFloatExtra("weight", 100F) }

    private val mSensorManager: SensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val mAccelerometer: Sensor by lazy { mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private var lastX: Float = Float.MIN_VALUE
    private var lastY: Float = Float.MIN_VALUE
    private var lastZ: Float = Float.MIN_VALUE
    private var lastUpdate: Long = 0
    private var mistakes = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accelerometer)
        startCountDown()
    }

    private fun startCountDown() {
        var countDownTime = 4
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                accelerometer_test_countdown.text = "$countDownTime"
                countDownTime--
            }

            override fun onFinish() {
                startTest()
            }
        }.start()
    }

    private fun startTest() {
        var currentTime = time - 1
        accelerometer_test_speed.text = "$mistakes mistakes"
        accelerometer_test_pbar.visibility = View.VISIBLE
        accelerometer_test_pbar.progress = 0
        accelerometer_test_countdown.visibility = View.GONE
        accelerometer_test_speed.visibility = View.VISIBLE
        accelerometer_test_time.visibility = View.VISIBLE
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        object : CountDownTimer(time * 1000, 1000) {
            override fun onTick(p0: Long) {
                accelerometer_test_time.text = "$currentTime"
                currentTime--
            }

            override fun onFinish() {
                mSensorManager.unregisterListener(this@AccelerometerActivity)
                accelerometer_test_time.text = "Finished"
                finishTest()
            }
        }.start()
    }

    private fun finishTest() {

        val score = (((Math.max(100 - mistakes * 10, 0))).toDouble() / 100 * maxValue).toLong()
        alert("You scored $score out of $maxValue.") {
            positiveButton("Continue") {
                val intent = Intent()
                intent.putExtra("score", score)
                intent.putExtra("weight", weight)
                setResult(ResultCodes.TEST_ACCELEROMETER_FINISH, intent)
                finish()
            }
        }.show()
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    //ACCELEROMETER SENSOR LISTENER
    override fun onAccuracyChanged(sensor: Sensor, p1: Int) {}

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val mySensor = sensorEvent.sensor
        if (mySensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = sensorEvent.values[0]
            val y = sensorEvent.values[1]
            val z = sensorEvent.values[2]

            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastUpdate) > 100) {
                val difTime = currentTime - lastUpdate
                lastUpdate = currentTime

                val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / difTime * 10000

                accelerometer_test_pbar.progress = (speed * 2).toInt()
                if (speed > 50) {
                    accelerometer_test_speed.setTextColor(Color.RED)
                    mistakes++
                    accelerometer_test_speed.text = "$mistakes mistakes."
                } else {
                    accelerometer_test_speed.setTextColor(Color.GREEN)
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }
}
