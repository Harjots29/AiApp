package com.harjot.aiapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.harjot.aiapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    var imgBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Set click listener on ImageView
        binding.ivCamera.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
        binding.btnSubmit.setOnClickListener {
            val prompt = binding.etQuestion.text.toString()
            val apiKey = "AIzaSyD3_Oj7qIaf-00hEX7lfRPQJVkxnu5pWag"

            val model = GenerativeModel(
                modelName = "gemini-1.5-flash-001",
                apiKey = apiKey
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Show the loader before making API call
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.txtResponse.text = "" // Clear previous response
                    }

                    // Make API Call
                    val response = model.generateContent(
                        content {
                            image(imgBitmap!!)
                            text("What is the object in this picture?")
                        }
                    )
                    val textResponse = response.text ?: "No response received"

                    // Hide loader and display response
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.txtResponse.text = textResponse
                    }
                } catch (e: Exception) {
                    Log.e("ChatBot", "Error: ${e.message}")

                    // Hide loader and show error message
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.txtResponse.text = "Error: ${e.message}"
                    }
                }
            }
        }

    }
    // Function to check camera permission
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request camera permission
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }
    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Function to open camera
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    // Handle the camera result
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            binding.ivCamera.setImageBitmap(imageBitmap)
            imgBitmap = imageBitmap
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 101
    }
}