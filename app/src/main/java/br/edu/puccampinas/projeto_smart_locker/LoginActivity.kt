package br.edu.puccampinas.projeto_smart_locker

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val loginButton = findViewById<Button>(R.id.btLogin)
        loginButton.setOnClickListener {
            validUser()
        }
        val mapButton = findViewById<ImageView>(R.id.btMap)
        mapButton.setOnClickListener{
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

    }
    private fun validUser() {
        val email = findViewById<EditText>(R.id.editUsuario).text.toString()
        val password = findViewById<EditText>(R.id.editSenha).text.toString()
        if (email.isBlank() or password.isBlank()){
            return
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                startActivity(Intent(this, ClientMainScreenActivity::class.java))
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Falha ao autenticar usuário: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}