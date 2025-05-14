package com.example.trashsense.upload

import HomeFragment
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.trashsense.HomeActivity
import com.example.trashsense.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class UploadFragment : Fragment() {

    private lateinit var postImage: ImageView
    private lateinit var postText: EditText
    private lateinit var fab: FloatingActionButton
    private lateinit var Pf_url :String
    private lateinit var pf_username :String

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String

    companion object {
        private var cloudinaryInitialized = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!cloudinaryInitialized) {
            val config = HashMap<String, String>().apply {
                put("cloud_name", "dhccxvbdw")
                put("api_key", "558765732587577")
                put("api_secret", "drQEUuQFDK_blgeKHKRQycUy9Mk")
            }
            try {
                MediaManager.init(requireContext(), config)
                cloudinaryInitialized = true
            } catch (e: Exception) {
                Log.e("Cloudinary", "Init error: ${e.message}")
            }
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        uid = auth.currentUser?.uid ?: "Unknown"

        if (uid != null) {
            db.collection("User").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Pf_url = document.getString("url") ?: ""
                        pf_username =document.getString("Username") ?: ""
                    }}}

        setupCameraLauncher()
        setupGalleryLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)

        postImage = view.findViewById(R.id.post_image)
        postText = view.findViewById(R.id.Post_text)
        fab = view.findViewById(R.id.floatingActionButton)

        fab.setOnClickListener {
            showImagePickerOptions()
        }


        return view
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Camera", "Gallery")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Choose image source")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val bitmap = it.data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    postImage.setImageBitmap(it)
                    imageUri = saveBitmapToTempFile(it)
                    uploadImageToCloudinary()
                }
            }
        }
    }

    private fun setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                imageUri = it.data?.data
                postImage.setImageURI(imageUri)
                uploadImageToCloudinary()
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            cameraLauncher.launch(intent)
        } else {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun uploadImageToCloudinary() {
        val uri = imageUri ?: return Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Toast.makeText(requireContext(), "Uploading...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    val progress = (bytes * 100 / totalBytes).toInt()
                    Log.d("Cloudinary", "Progress: $progress%")
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val publicId = resultData?.get("public_id") as? String
                    val url = resultData?.get("secure_url") as? String
                    val text = postText.text.toString()



                    val postMap = hashMapOf(
                        "publicId" to publicId,
                        "url" to url,
                        "Profile_url" to Pf_url,
                        "Username" to pf_username,
                        "text" to text
                    )

                    db.collection("Posts").document("Data").collection("Aayush").add(postMap)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Post uploaded", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(requireContext(),HomeActivity::class.java))
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to save post", Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(requireContext(), "Upload error: ${error?.description}", Toast.LENGTH_LONG).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(requireContext(), "Upload rescheduled", Toast.LENGTH_SHORT).show()
                }
            })
            .dispatch()
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap): Uri? {
        return try {
            val tempFile = File.createTempFile("image", ".jpg", requireContext().cacheDir)
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
            }
            Uri.fromFile(tempFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
