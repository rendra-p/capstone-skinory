package com.capstone.skinory.ui.analysis

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.capstone.skinory.data.UserPreferences
import com.capstone.skinory.data.remote.retrofit.ApiModelConfig
import com.capstone.skinory.ui.MainActivity
import com.capstone.skinory.databinding.ActivityAnalysisBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date

class AnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnalysisBinding
    private lateinit var userPreference: UserPreferences
    private lateinit var progressBar: ProgressBar
    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 200
    private lateinit var photoURI: Uri
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreferences(this)
        progressBar = binding.progressBar

        binding.btnCamera.setOnClickListener {
            openCamera()
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        binding.btnAnalysis.setOnClickListener {
            if (imageUri != null) {
                uploadImageAndAnalyze()
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }

        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable("imageUri")
            imageUri?.let {
                compressAndSetImage(it)
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                val photoFile: File? = createImageFile()
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_$timeStamp",
            ".jpg",
            storageDir
        )
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    compressAndSetImage(photoURI)
                }
                GALLERY_REQUEST_CODE -> {
                    imageUri = data?.data
                    imageUri?.let {
                        compressAndSetImage(it)
                    }
                }
            }
        }
    }

    private fun compressAndSetImage(uri: Uri) {
        val bitmap = decodeSampledBitmapFromUri(uri, 1024, 1024)
        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            var quality = 100

            do {
                outputStream.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                quality -= 5
            } while (outputStream.size() / 1024 > 1024 && quality > 0)

            val compressedBitmap = BitmapFactory.decodeStream(ByteArrayInputStream(outputStream.toByteArray()))
            binding.imgUpload.setImageBitmap(compressedBitmap)
        } else {
            Toast.makeText(this, "Failed to load the image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decodeSampledBitmapFromUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        options.inJustDecodeBounds = false
        return BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun uploadImageAndAnalyze() {
        val userId = userPreference.getUserId()
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        binding.btnAnalysis.isEnabled = false

        val file = imageUri?.let { uri ->
            File(getRealPathFromURI(uri) ?: return@let null)
        }

        if (file == null) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            binding.btnAnalysis.isEnabled = true
            return
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ApiModelConfig.getApiService().uploadImage(
                        userId = userId,
                        image = body
                    )

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        binding.btnAnalysis.isEnabled = true

                        val intent = Intent(this@AnalysisActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    binding.btnAnalysis.isEnabled = true

                    val errorMessage = when (e) {
                        is SocketTimeoutException -> "Connection timed out. Please check your internet connection."
                        is UnknownHostException -> "No internet connection."
                        is IOException -> "Network error occurred: ${e.message}"
                        else -> "Upload failed: ${e.message}"
                    }

                    Toast.makeText(this@AnalysisActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        return cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            it.getString(columnIndex)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("imageUri", imageUri)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}