package com.example.trashsense.profile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.trashsense.HomeActivity
import com.example.trashsense.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditProfileBottomSheet(
    private val currentName: String,
    private val onSave: (String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var imageUri: Uri
    private lateinit var usernameField: EditText
    private lateinit var saveBtn: Button
    private lateinit var forgotPassBtn: MaterialButton
    private lateinit var changePicBtn: FloatingActionButton

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                saveBitmapToTempFile(it)?.let { uri ->
                    imageUri = uri
                    Glide.with(requireContext()).load(imageUri).circleCrop().into(profileImageView)
                }
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let {
                imageUri = it
                Glide.with(requireContext()).load(imageUri).circleCrop().into(profileImageView)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.bottomsheet_edit_username, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        usernameField = view.findViewById(R.id.edit_username)
        saveBtn = view.findViewById(R.id.save_button)
        forgotPassBtn = view.findViewById(R.id.forgot_password_button)
        changePicBtn = view.findViewById(R.id.edit_pic_button)
        profileImageView = view.findViewById(R.id.profile_image)
        val newPasswordField =view.findViewById<TextInputEditText>(R.id.new_password)
        val  currentPasswordField=view.findViewById<TextInputEditText>(R.id.current_password)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        uid = auth.currentUser?.uid.toString()
        usernameField.setText(currentName)

        if (!cloudinaryInitialized) {
            val config = HashMap<String, String>()
            config["cloud_name"] = "dhccxvbdw"
            config["api_key"] = "558765732587577"
            config["api_secret"] = "drQEUuQFDK_blgeKHKRQycUy9Mk"
            try {
                MediaManager.init(requireContext(), config)
                cloudinaryInitialized = true
            } catch (e: Exception) {
                Toast.makeText(context, "Cloudinary Init Failed: ${e.message}", Toast.LENGTH_LONG).show()
                return
            }
        }

        saveBtn.setOnClickListener {
            val newName = usernameField.text.toString().trim()
            val newPassword = newPasswordField.text.toString().trim()
            val currentPassword = currentPasswordField.text.toString().trim()
            val user = FirebaseAuth.getInstance().currentUser

            if (newName.isEmpty()) {
                Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!newPassword.isNullOrEmpty() && !currentPassword.isNullOrEmpty() && user != null) {
                val email = user.email

                if (!email.isNullOrEmpty()) {
                    val credential = EmailAuthProvider.getCredential(email, currentPassword)

                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            user.updatePassword(newPassword)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()

                                    if (::imageUri.isInitialized) {
                                        uploadImageToCloudinary(newName)
                                    } else {
                                        saveUsernameOnly(newName)
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to update password: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Re-authentication failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }

                } else {
                    Toast.makeText(context, "User email not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (::imageUri.isInitialized) {
                    uploadImageToCloudinary(newName)
                } else {
                    saveUsernameOnly(newName)
                }
            }

        }



        forgotPassBtn.setOnClickListener {
            val passwordInputLayout = requireView().findViewById<TextInputLayout>(R.id.password_input_layout)
            val ctpas =requireView().findViewById<TextInputLayout>(R.id.current_password_input_layout)
                ctpas.visibility = View.VISIBLE
               passwordInputLayout.visibility = View.VISIBLE
        }


        changePicBtn.setOnClickListener {
            showImageOptionDialog()
        }
    }

    private fun showImageOptionDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Change Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(Manifest.permission.CAMERA),
                                100
                            )
                        } else {
                            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            cameraLauncher.launch(takePictureIntent)
                        }
                    }

                    1 -> {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        intent.type = "image/*"
                        galleryLauncher.launch(intent)
                    }
                }
            }.show()
    }

    private fun uploadImageToCloudinary(username: String) {
        try {
            MediaManager.get().upload(imageUri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Toast.makeText(context, "Upload Started", Toast.LENGTH_SHORT).show()
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val publicId = resultData?.get("public_id") as String?
                        val url = resultData?.get("secure_url") as String?
                        val userMap = hashMapOf(
                            "publicId" to publicId,
                            "url" to url,
                            "Username" to username
                        )
                        db.collection("User").document(uid).update(userMap as Map<String, Any>).addOnSuccessListener {
                            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                            onSave(username)
                            dismiss()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to update Firestore", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Toast.makeText(context, "Upload failed: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } catch (e: Exception) {
            Toast.makeText(context, "Upload Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUsernameOnly(username: String) {
        db.collection("User").document(uid).update("Username", username).addOnSuccessListener {
            Toast.makeText(context, "Username updated", Toast.LENGTH_SHORT).show()
            onSave(username)
            dismiss()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to update username", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap): Uri? {
        return try {
            val tempFile = File.createTempFile("temp_img", ".jpg", requireContext().cacheDir)
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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
