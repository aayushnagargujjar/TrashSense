package com.example.trashsense

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.trashsense.Question.Question1
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.lang.Exception

class UsernameActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db:FirebaseFirestore
    private lateinit var uid :String
    private lateinit var profileImageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var usernameEditText: EditText
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null  // To store the selected image URI

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_username)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth =FirebaseAuth.getInstance()
        db =FirebaseFirestore.getInstance()
        uid = auth.currentUser?.uid.toString()
        profileImageView = findViewById(R.id.profileImageView)
        uploadButton = findViewById(R.id.uploadButton)
        usernameEditText = findViewById(R.id.Username)

        // Initialize Cloudinary (in Application class is best, but here with check)
        if (!cloudinaryInitialized) {
            val config = HashMap<String, String>()
            config["cloud_name"] = "dhccxvbdw"
            config["api_key"] = "558765732587577"
            config["api_secret"] = "drQEUuQFDK_blgeKHKRQycUy9Mk"
            try {
                MediaManager.init(this, config)
                cloudinaryInitialized = true
            } catch (e: Exception) {
                Log.e("Cloudinary", "Initialization Error: ${e.message}", e)
                Toast.makeText(this, "Cloudinary Init Failed: ${e.message}", Toast.LENGTH_LONG).show()
                // Handle the error appropriately, e.g., prevent further Cloudinary operations
                return  // IMPORTANT:  Return if init fails!
            }
        }
        
        setupCameraResultLauncher()
        setupGalleryResultLauncher()

        findViewById<Button>(R.id.cameraButton).setOnClickListener {
            takeImageFromCamera()
        }
        findViewById<Button>(R.id.galleryButton).setOnClickListener {
            selectImageFromGallery()
        }

        uploadButton.setOnClickListener {
            uploadImageToCloudinary()
        }
    }

    private fun setupCameraResultLauncher() {
        cameraResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val imageBitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
                    imageBitmap?.let {
                        profileImageView.setImageBitmap(it)
                        // Save the bitmap to a file and get the Uri.  This is crucial for Cloudinary!
                        imageUri = saveBitmapToTempFile(it) // Implement this function
                        Log.d("Camera", "Image URI: $imageUri")
                    }
                } else {
                    Toast.makeText(this, "Camera operation cancelled", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupGalleryResultLauncher() {
        galleryResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    data?.data?.let { uri ->
                        imageUri = uri
                        profileImageView.setImageURI(uri)
                        Log.d("Gallery", "Image URI: $uri")
                    }
                } else {
                    Toast.makeText(this, "Gallery operation cancelled", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun takeImageFromCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            cameraResultLauncher.launch(takePictureIntent)
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryResultLauncher.launch(intent)
    }

    private fun uploadImageToCloudinary() {
        val uri = imageUri
        if (uri != null) {
            try {
                MediaManager.get().upload(uri)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            if (Looper.myLooper() != Looper.getMainLooper()) {
                                Log.e("Cloudinary", "onStart is NOT on main thread!  requestId=$requestId")
                            }
                            Log.d("Cloudinary", "Upload started for request: $requestId")
                            runOnUiThread {
                                Toast.makeText(this@UsernameActivity, "Upload Started...", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            if (Looper.myLooper() != Looper.getMainLooper()) {
                                Log.e("Cloudinary", "onProgress is NOT on main thread! requestId=$requestId")
                            }
                            val progress = (bytes * 100 / totalBytes).toInt()
                            Log.d("Cloudinary", "Upload progress ($requestId): $progress%")
                            // You could update a progress bar here, but ensure it's on the main thread.
                            runOnUiThread {
                                //updateProgressBar(progress)
                            }
                        }

                        override fun onSuccess(
                            requestId: String?,
                            resultData: MutableMap<Any?, Any?>?
                        ) {
                            if (Looper.myLooper() != Looper.getMainLooper()) {
                                Log.e("Cloudinary", "onSuccess is NOT on main thread! requestId=$requestId")
                            }
                            Log.d("Cloudinary", "Upload successful ($requestId): $resultData")
                            runOnUiThread {
                                Toast.makeText(
                                    this@UsernameActivity,
                                    "Uploaded! ${resultData?.get("public_id")}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            val publicId = resultData?.get("public_id") as String?
                            val url = resultData?.get("secure_url") as String?
                            val username = usernameEditText.text.toString()

                            val userMap = hashMapOf(
                                "publicId" to publicId,
                                "url" to url,
                                "Username" to username
                            )
                            db.collection("User").document(uid).set(userMap).addOnSuccessListener {
                                Toast.makeText(this@UsernameActivity,"Username and profilepic store succesfully",Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@UsernameActivity,Question1::class.java))
                            }.addOnFailureListener{
                                Toast.makeText(this@UsernameActivity,"Username Not store in firebasefirestore ",Toast.LENGTH_SHORT).show()
                            }


                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            if (Looper.myLooper() != Looper.getMainLooper()) {
                                Log.e("Cloudinary", "onError is NOT on main thread! requestId=$requestId")
                            }
                            var errorMessage = "Upload Failed"
                            errorMessage += ": ${error.description}"
                            Log.e("Cloudinary", "Upload error ($requestId): $errorMessage")
                            runOnUiThread {
                                Toast.makeText(this@UsernameActivity, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            if (Looper.myLooper() != Looper.getMainLooper()) {
                                Log.e("Cloudinary", "onReschedule is NOT on main thread! requestId=$requestId")
                            }
                            var rescheduleMessage = "Upload Rescheduled"
                            if (error != null && error.description != null) {
                                rescheduleMessage += ": ${error.description}"
                            } else {
                                rescheduleMessage += ": Unknown error"
                            }
                            Log.e("Cloudinary", "Upload rescheduled ($requestId): $rescheduleMessage")
                            runOnUiThread {
                                Toast.makeText(this@UsernameActivity, rescheduleMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                    })
                    .dispatch()
                    .let { requestId ->
                        Log.d("Cloudinary", "Request ID: $requestId")
                    }
            } catch (e: Exception) {
                Log.e("Cloudinary", "Upload Failed: ${e.message}", e)
                Toast.makeText(this, "Upload Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Please select an image to upload", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToTempFile(bitmap: android.graphics.Bitmap): Uri? {
        return try {
            val tempFile = createTempFile("temp_image", ".jpg", cacheDir)
            val outputStream = java.io.FileOutputStream(tempFile)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(tempFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private var cloudinaryInitialized = false
    }
}

