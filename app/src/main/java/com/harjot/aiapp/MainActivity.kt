package com.harjot.aiapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.ai.client.generativeai.GenerativeModel
import com.harjot.aiapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            val prompt = binding.etQuestion.text.toString()
            val apiKey = "AIzaSyD3_Oj7qIaf-00hEX7lfRPQJVkxnu5pWag"

            val model = GenerativeModel(
                modelName = "gemini-pro",
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
                    val response = model.generateContent(prompt)
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
}