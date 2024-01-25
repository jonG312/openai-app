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

        // Obteniendo referencias a vistas
        val etQuestion = findViewById<EditText>(R.id.etQuestion)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val txtResponse = findViewById<TextView>(R.id.txtResponse)

        // Definiendo el evento clic del botón
        btnSubmit.setOnClickListener {
            // Obteniendo la pregunta del EditText y mostrándola en un Toast
            val question = etQuestion.text.toString().trim()
            Toast.makeText(this, question, Toast.LENGTH_SHORT).show()

            // Verificando si la pregunta no está vacía
            if (question.isNotEmpty()) {
                // Llamando a la función para obtener una respuesta y actualizando la vista con la respuesta
                getResponse(question) { response ->
                    runOnUiThread {
                        txtResponse.text = response
                    }
                }
            }
        }
    }

    // Función para obtener la respuesta del modelo GPT-3
    fun getResponse(question: String, callback: (String) -> Unit) {
        val apiKey = "YOUR_API_KEY" // Reemplaza con tu clave de API de OpenAI
        val url = "https://api.openai.com/v1/engines/text-davinci-003/completions"

        // Creando el cuerpo de la solicitud en formato JSON
        val requestBody = """
            {
                "prompt": "$question",
                "max_tokens": 500,
                "temperature": 0
            }
        """.trimIndent()

        // Construyendo la solicitud HTTP usando OkHttp
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        // Realizando la llamada asíncrona a la API de OpenAI
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejando fallos en la llamada a la API
                Log.e("error", "API failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                // Procesando la respuesta de la API
                val body = response.body?.string()

                if (body != null) {
                    Log.v("data", body)
                } else {
                    Log.v("data", "empty")
                }

                // Parseando la respuesta JSON
                val jsonObject = JSONObject(body)
                val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                val textResult = jsonArray.getJSONObject(0).getString("text")

                // Llamando al callback con la respuesta para actualizar la interfaz de usuario
                callback(textResult)
            }
        })
    }
}