package com.moviles.psychoapp.activities

import android.Manifest
import android.annotation.TargetApi
import android.app.Dialog
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.moviles.psychoapp.R
import com.moviles.psychoapp.adapters.MyExamsAdapter
import com.moviles.psychoapp.utils.FingerprintHandler
import com.moviles.psychoapp.utils.RequestCodes
import com.moviles.psychoapp.world.BriefExam
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fingerprint_dialog.*
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    val mAuth = FirebaseAuth.getInstance()
    val myExams = ArrayList<BriefExam>()
    var myExamsAdapter: MyExamsAdapter? = null
    private var keyStore: KeyStore? = null
    // Variable used for storing the key in the Android Keystore container
    private val KEY_NAME = "androidHive"
    private var cipher: Cipher? = null
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getMyExams()
    }

    override fun onStart() {
        mAuth.currentUser!!.reload()
        if (mAuth.currentUser == null) {
            goToLogin()
        }
        checkUserIsStillLogged()
        setupFingerprintScanner()
        super.onStart()
    }

    private fun getMyExams() {
        FirebaseDatabase.getInstance().getReference("candidates")
                .child(mAuth.currentUser!!.uid).child("exams").addChildEventListener(
                object: ChildEventListener {
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
                                myExamsAdapter = MyExamsAdapter(myExams, R.layout.item_my_exams) {

                                }
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

        fab_scan_exam.setOnClickListener {
            mAuth.signOut()
            goToLogin()
        }
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

    fun hideFingerprintDialog() {
        val handler = Handler()
        handler.post {
            dialog.dialog_img_fingerprint.setColorFilter(ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_light))
            handler.postDelayed({
                dialog.hide()
                dialog.dismiss()
            }, 150)
        }
    }

    fun showFingerprintError() {
        val handler = Handler()
        handler.post({
            dialog.dialog_img_fingerprint.setColorFilter(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_light))
            handler.postDelayed({
                dialog.dialog_img_fingerprint.setColorFilter(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                handler.postDelayed({
                    dialog.dialog_img_fingerprint.setColorFilter(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_light))
                    handler.postDelayed({
                        dialog.dialog_img_fingerprint.setColorFilter(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                    }, 150)
                }, 150)
            }, 150)
        })
    }

    private fun setupFingerprintScanner() {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val fingerprintManager = FingerprintManagerCompat.from(this@MainActivity)

        if (!fingerprintManager.isHardwareDetected) {

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.USE_FINGERPRINT), RequestCodes.LOGIN_PERMISSION_FINGERPRINT)
            } else {
                if (!fingerprintManager.hasEnrolledFingerprints()) {

                } else {
                    if (!keyguardManager.isKeyguardSecure) {

                    } else {
                        generateKey()
                        if (cipherInit()) {
                            dialog = Dialog(this@MainActivity)
                            dialog.setContentView(R.layout.fingerprint_dialog)
                            dialog.setCancelable(false)
                            dialog.show()

                            val cryptoObject = FingerprintManagerCompat.CryptoObject(cipher)
                            val helper = FingerprintHandler(this, lyt_main)
                            helper.startAuth(fingerprintManager, cryptoObject)
                        }
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val keyGenerator: KeyGenerator
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        }

        try {
            keyStore!!.load(null)
            keyGenerator.init(KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build())
            keyGenerator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun cipherInit(): Boolean {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore!!.load(
                    null)
            val key = keyStore!!.getKey(KEY_NAME, null) as SecretKey
            cipher!!.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RequestCodes.LOGIN_PERMISSION_FINGERPRINT -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupFingerprintScanner()
                }
            }
        }
    }
}
