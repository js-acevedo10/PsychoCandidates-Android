package com.moviles.psychoapp.activities

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.moviles.psychoapp.R
import com.moviles.psychoapp.adapters.MyExamsAdapter
import com.moviles.psychoapp.utils.BitmapDecoder
import com.moviles.psychoapp.utils.FingerprintHandler
import com.moviles.psychoapp.utils.RequestCodes
import com.moviles.psychoapp.world.BriefExam
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fingerprint_dialog.*
import java.io.File
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
    private var myExamsAdapter: MyExamsAdapter? = null

    //Huella
    private lateinit var keyStore: KeyStore
    private val KEY_NAME = "androidHive"
    private lateinit var cipher: Cipher
    private lateinit var dialog: Dialog

    //Barcode Scanner
    private val detector: BarcodeDetector by lazy {
        BarcodeDetector.Builder(applicationContext)
                .setBarcodeFormats(Barcode.DATA_MATRIX or Barcode.QR_CODE)
                .build()
    }
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getMyExams()
        setupFingerprintScanner()
    }

    override fun onStart() {
        mAuth.currentUser!!.reload()
        if (mAuth.currentUser == null) {
            goToLogin()
        }
        checkUserIsStillLogged()
        super.onStart()
    }

    override fun onBackPressed() {
        finish()
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
                            val bE = ds.getValue(BriefExam::class.java)!!
                            bE.id = ds.key
                            myExams.add(bE)
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
                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                        for (i in myExams.indices) {
                            if (myExams[i].id == p0.key) {
                                val bE = p0.getValue(BriefExam::class.java)!!
                                bE.id = p0.key
                                myExams[i] = bE
                                myExamsAdapter!!.notifyItemChanged(i)
                                break
                            }
                        }
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {
                        for (i in myExams.indices) {
                            if (myExams[i].id == p0.key) {
                                myExams.removeAt(i)
                                myExamsAdapter!!.notifyItemRemoved(i)
                                break
                            }
                        }
                    }
                }
        )

        fab_scan_exam.setOnClickListener {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), RequestCodes.BARCODE_PERMISSION_CAMERA)
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

    private fun scanBarcode() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photo = File(Environment.getExternalStorageDirectory(), "barcode.jpg")
        imageUri = Uri.fromFile(photo)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, RequestCodes.BARCODE_CAMERA_SCAN)
    }

    private fun launchMediaScanIntent() {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = imageUri
        this.sendBroadcast(mediaScanIntent)
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
            keyStore.load(null)
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
            keyStore.load(null)
            val key = keyStore.getKey(KEY_NAME, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.BARCODE_CAMERA_SCAN -> {
                if (resultCode == Activity.RESULT_OK) {
                    launchMediaScanIntent()
                    try {
                        val bitmap = BitmapDecoder.decodeBitmapUri(this@MainActivity, imageUri)
                        if (detector.isOperational) {
                            val frame = Frame.Builder().setBitmap(bitmap).build()
                            val barcodes = detector.detect(frame)
                            for (index in 0 until barcodes.size()) {
                                val code = barcodes.valueAt(index)
                                println("El codigo es ${code.displayValue}")
                                FirebaseDatabase.getInstance().getReference("exams")
                                        .child(code.displayValue).addListenerForSingleValueEvent(
                                        object : ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError?) {}
                                            override fun onDataChange(ds: DataSnapshot) {
                                                if (ds.exists()) {
                                                    startActivity(Intent(this@MainActivity, ExamDetailsActivity::class.java).putExtra("examId", ds.key))
                                                }
                                            }
                                        }
                                )
                            }
                        }
                    } catch (e: Exception) {

                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RequestCodes.LOGIN_PERMISSION_FINGERPRINT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupFingerprintScanner()
                }
            }
            RequestCodes.BARCODE_PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanBarcode()
                }
            }
        }
    }
}
