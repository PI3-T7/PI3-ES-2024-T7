package br.edu.puccampinas.projeto_smart_locker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class VerifyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)
        val emailText = findViewById<TextView>(R.id.emailText)
        val user = FirebaseAuth.getInstance().currentUser
        emailText.text = user?.email
        val okButton = findViewById<Button>(R.id.butOk)
        FirebaseAuth.getInstance().signOut()
        okButton.setOnClickListener {
            finish()
        }
    }
}