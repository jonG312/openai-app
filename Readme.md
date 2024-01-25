## Environment Configuration

1. **Open Android Studio** -> **Create new project** -> **Select Kotlin**

## Writing dependencies:

Open `build.gradle.kts (:app)`:
```kotlin
dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}
```

## Create the app

`Activity_Main.xml`:

```<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="20dp"
    tools:context=".MainActivity">

    <!-- EditText to write the question -->
    <EditText
        android:id="@+id/etQuestion"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Type your question here"
        android:textSize="20sp" />

    <!-- Button to send the question -->
    <Button
        android:id="@+id/btnSubmit"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Submit"
        android:layout_marginTop="20dp" />

    <!-- TextView to show the answer -->
    <TextView
        android:id="@+id/txtResponse"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Your response will appear here!"
        android:textSize="20sp"
        android:layout_marginTop="5dp"
        android:layout_marginEnd="5dp"
        android:layout_marginBottom="5dp"
        android:padding="5dp" />

    <!-- Additional LinearLayout -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"/>
</LinearLayout>
```

## Coding the class Main_Activity.kt
`Getting view references`:

```
val etQuestion = findViewById<EditText>(R.id.etQuestion)
val btnSubmit = findViewById<Button>(R.id.btnSubmit)
val txtResponse = findViewById<TextView>(R.id.txtResponse)
```

`Defining the event with a click`:

```
btnSubmit.setOnClickListener {
    // Getting the question from the EditText and displaying it in a Toast
    val question = etQuestion.text.toString().trim()
    Toast.makeText(this, question, Toast.LENGTH_SHORT).show()

    // Verifying if the question is not empty
    if (question.isNotEmpty()) {
        // Calling the function to get a response and updating the view with the response
        getResponse(question) { response ->
            runOnUiThread {
                txtResponse.text = response
            }
        }
    }
}
```

`Creating the function to get the question from the GPT-3 model`:

```
fun getResponse(question: String, callback: (String) -> Unit) {
    val apiKey = "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" // Replace with your OpenAI API key
    val url = "https://api.openai.com/v1/engines/text-davinci-003/completions"

    // Creating the request body in JSON format
    val requestBody = """
        {
            "prompt": "$question",
            "max_tokens": 500,
            "temperature": 0
        }
    """.trimIndent()

    // Building the HTTP request using OkHttp
    val request = Request.Builder()
        .url(url)
        .addHeader("Content-Type", "application/json")
        .addHeader("Authorization", "Bearer $apiKey")
        .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    // Making the asynchronous call to the OpenAI API
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Handling API call failures
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
```
## App Design:

![app design](https://github.com/jonG312/openai-app/blob/main/images/OpenAi_app.png)



