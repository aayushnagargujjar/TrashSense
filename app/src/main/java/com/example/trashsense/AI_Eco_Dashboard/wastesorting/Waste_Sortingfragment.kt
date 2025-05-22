package com.example.trashsense.AI_Eco_Dashboard.wastesorting

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.trashsense.R
import com.example.trashsense.ml.OptimizedModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

class Waste_Sortingfragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var textPrediction: TextView
    private lateinit var textMappedCategory: TextView
    private lateinit var btnUpload: Button

    private val GALLERY_REQUEST = 1001
    private val CAMERA_REQUEST = 1002
    private var currentPhotoPath: String = ""

    private val labels = listOf(
        "metal cans", "food waste", "leaf waste", "paper waste",
        "plastic bottles", "ewaste", "wood waste", "plastic bags"
    )

    private val wasteTypeMap = mapOf(
        "metal cans" to "Recyclable",
        "paper waste" to "Recyclable",
        "plastic bottles" to "Recyclable",
        "plastic bags" to "Recyclable",
        "wood waste" to "Recyclable",
        "leaf waste" to "Organic",
        "food waste" to "Organic",
        "ewaste" to "Hazardous"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_waste__sortingfragment, container, false)

        imagePreview = view.findViewById(R.id.imagePreview)
        textPrediction = view.findViewById(R.id.textPrediction)
        textMappedCategory = view.findViewById(R.id.textMappedCategory)
        btnUpload = view.findViewById(R.id.btnUploadImage)

        btnUpload.setOnClickListener {
            showImagePickerDialog()
        }

        return view
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Capture Image", "Select from Gallery")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Image Source")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireActivity().packageName}.provider",  // Must match Manifest authority
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                // Grant URI permission to camera activity
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivityForResult(intent, CAMERA_REQUEST)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = requireActivity().cacheDir
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST -> {
                    val imageUri: Uri? = data?.data
                    val bitmap = imageUri?.let { uri ->
                        if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                        } else {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, uri)
                            ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
                        }
                    }
                    bitmap?.let {
                        imagePreview.setImageBitmap(it)
                        classifyImage(it)
                    }
                }

                CAMERA_REQUEST -> {
                    val file = File(currentPhotoPath)
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(requireActivity().contentResolver, Uri.fromFile(file))
                        ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
                    } else {
                        MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, Uri.fromFile(file))
                    }
                    imagePreview.setImageBitmap(bitmap)
                    classifyImage(bitmap)
                }
            }
        }
    }

    private fun classifyImage(bitmap: Bitmap) {
        val resized = Bitmap.createScaledBitmap(bitmap, 299, 299, true)

        val byteBuffer = ByteBuffer.allocateDirect(4 * 299 * 299 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(299 * 299)
        resized.getPixels(intValues, 0, 299, 0, 0, 299, 299)

        for (pixelValue in intValues) {
            val r = ((pixelValue shr 16) and 0xFF) / 255.0f
            val g = ((pixelValue shr 8) and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        val inputFeature = TensorBuffer.createFixedSize(intArrayOf(1, 299, 299, 3), DataType.FLOAT32)
        inputFeature.loadBuffer(byteBuffer)

        val model = OptimizedModel.newInstance(requireContext())
        val outputs = model.process(inputFeature)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val confidences = outputFeature0.floatArray
        val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1
        val resultLabel = if (maxIndex in labels.indices) labels[maxIndex] else "Unknown"
        val mappedType = wasteTypeMap[resultLabel] ?: "Unknown"

        textPrediction.text = "Waste Category: $resultLabel"
        textMappedCategory.text = "Type: $mappedType"

        model.close()
    }
}
