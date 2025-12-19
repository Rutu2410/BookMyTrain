package com.example.trainn

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var togglePassword: ImageView
    private lateinit var loginLink: TextView
    private lateinit var requestQueue: RequestQueue
    private lateinit var sharedPreferences: SharedPreferences

    private val BASE_URL = "http://192.168.209.246/database1/login.php"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Auto-login
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val roleSaved = sharedPreferences.getString("role", "")
        if (isLoggedIn) {
            val intent = if (roleSaved == "admin") Intent(this, adminhome::class.java)
            else Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        togglePassword = findViewById(R.id.togglePassword)
        loginLink = findViewById(R.id.loginLink)
        requestQueue = Volley.newRequestQueue(this)

        // Toggle password visibility
        var isPasswordVisible = false
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            password.inputType = if (isPasswordVisible) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            togglePassword.setImageResource(if (isPasswordVisible) R.drawable.eye1 else R.drawable.eyeclosed)
            password.setSelection(password.text.length)
        }

        loginButton.setOnClickListener { loginUser() }

        loginLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val enteredEmail = email.text.toString().trim()
        val enteredPassword = password.text.toString().trim()

        if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
            Toast.makeText(this, "Enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("LoginDebug", "Sending email=$enteredEmail, password=$enteredPassword")

        val stringRequest = object : StringRequest(
            Request.Method.POST, BASE_URL,
            Response.Listener { response ->
                Log.d("ServerResponse", response)
                try {
                    val jsonResponse = JSONObject(response.trim())
                    val status = jsonResponse.optString("status", "error")

                    if (status == "success") {
                        val userId = jsonResponse.getInt("user_id")
                        val username = jsonResponse.getString("username")
                        val email = jsonResponse.getString("email")
                        val role = jsonResponse.getString("role")

                        saveUserData(userId, username, email, role)
                        Toast.makeText(this, "Welcome, $username!", Toast.LENGTH_SHORT).show()

                        val intent = if (role == "admin") Intent(this, adminhome::class.java)
                        else Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        val message = jsonResponse.optString("message", "Invalid credentials")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Server returned invalid response", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("LoginError", "Error: ${error.message}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "email" to enteredEmail,
                    "password" to enteredPassword
                )
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun saveUserData(userId: Int, username: String, email: String, role: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putInt("userId", userId)
        editor.putString("username", username)
        editor.putString("email", email)
        editor.putString("role", role)
        editor.apply()
    }
}
