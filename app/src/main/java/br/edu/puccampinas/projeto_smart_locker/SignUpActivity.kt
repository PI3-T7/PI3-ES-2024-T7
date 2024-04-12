package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val signUpButton = findViewById<Button>(R.id.bt_signup)
        signUpButton.setOnClickListener {
            validData()
        }
        val arrow = findViewById<ImageView>(R.id.arrow)
        arrow.setOnClickListener {
            finish()
        }
    }

    private fun validData() {
        val values = mutableListOf<String>()
        values.add(findViewById<EditText>(R.id.editName).text.toString())
        values.add(findViewById<EditText>(R.id.editCpf).text.toString())
        values.add(findViewById<EditText>(R.id.editBirthDate).text.toString())
        values.add(findViewById<EditText>(R.id.editPhone).text.toString())
        values.add(findViewById<EditText>(R.id.editEmail).text.toString())
        values.add(findViewById<EditText>(R.id.editPassword).text.toString())
        for (i in values) {
            if (i.isBlank()) {
                Toast.makeText(this, "Por favor, preencha todos os campos antes de prosseguir.", Toast.LENGTH_SHORT).show()
                return
            }
        }
        cadastrarUsuario(values)
    }

    private fun cadastrarUsuario(informations: List<String>) {
        val autenticacao = FirebaseAuth.getInstance()
        autenticacao.createUserWithEmailAndPassword(
            informations[4], informations[5]
        ).addOnSuccessListener {
            authResult ->
            authResult.user?.sendEmailVerification()?.addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    val emailVerificationScreen = Intent(this, VerifyActivity::class.java)
                    startActivity(emailVerificationScreen)
                } else {
                    Toast.makeText(this, "Falha ao enviar email de verificação, tente outro endereço de email!", Toast.LENGTH_LONG).show()
                }
            }
        }.addOnFailureListener {
            exception -> Toast.makeText(this, "Falha ao cadastrar usuário: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }
}