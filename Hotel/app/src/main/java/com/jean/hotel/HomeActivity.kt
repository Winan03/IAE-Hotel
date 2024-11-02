package com.jean.hotel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
enum class ProviderType{
    BASIC,
    GOOGLE
}

class Activity_home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bundle=intent.extras
        val email=bundle?.getString("email")
        val provider=bundle?.getString("provider")
        setup (email ?:"",provider?:"")
        val prefs =  getSharedPreferences(getString(R.string.prefs_file),Context.MODE_PRIVATE).edit()
        prefs.putString("email",email)
        prefs.putString("provider",provider)
        prefs.apply()

    }




    private fun setup(email:String,provider:String){
        title = "Inicio"
        val emailTextView = findViewById<TextView>(R.id.emailtextView)
        val providerTextView = findViewById<TextView>(R.id.providertextView)
        emailTextView.text=email
        providerTextView.text=provider
        val logOutButton=findViewById<Button>(R.id.logOutButton)
        logOutButton.setOnClickListener{
            val prefs =  getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressedDispatcher.onBackPressed()
        }
    }

}