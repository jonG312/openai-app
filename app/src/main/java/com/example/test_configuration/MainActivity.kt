package com.example.test_configuration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Getting references to views
        val etQuestion = findViewById<EditText>(R.id.etQuestion)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val txtResponse = findViewById<TextView>(R.id.txtResponse)

        // Defining the button click event
        btnSubmit.setOnClickListener {
            // Getting the question from the EditText and displaying it in a Toast
            val question = etQuestion.text.toString().trim()
            Toast.makeText(this, question, Toast.LENGTH_SHORT).show()

            // Checking if the question is not empty
            if (question.isNotEmpty()) {
                // Calling the function to get a response and updating the view with the response
                getResponse(question) { response ->
                    runOnUiThread {
                        txtResponse.text = response
                    }
                }
            }
        }
    }

    // Function to obtain the response of the GPT-3 model
    fun getResponse(question: String, callback: (String) -> Unit) {
        val apiKey = "YOUR_API_KEY" // Reemplaza con tu clave de API de OpenAI
        val url = "https://api.openai.com/v1/engines/text-davinci-003/completions"

        // Creating the request body in JSON format
        val requestBody = """
            {
                "prompt": "$question",
                "max_tokens": 500,
                "temperature": 0
            }
        """.trimIndent()

        // Building HTTP request using OkHttp
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        // Making the asynchronous call to the OpenAI API
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejando fallos en la llamada a la API
                Log.e("error", "API failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                // Processing the API response
                val body = response.body?.string()

                if (body != null) {
                    Log.v("data", body)
                } else {
                    Log.v("data", "empty")
                }

                // Parsing the JSON response
                val jsonObject = JSONObject(body)
                val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                val textResult = jsonArray.getJSONObject(0).getString("text")

                // Calling the callback with the response to update the UI
                callback(textResult)
            }
        })
    }
}