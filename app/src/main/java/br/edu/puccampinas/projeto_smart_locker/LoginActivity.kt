package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    // Configuração do ViewBinding
    private val binding by lazy { ActivityLoginBinding.inflate( layoutInflater ) }
    // Configuração do FirebaseAuth
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding){
            btLogin.setOnClickListener {
                validUser(editUsuario.text.toString(), editSenha.text.toString())
            }
            btMap.setOnClickListener {
                startActivity(Intent(this@LoginActivity, MapActivity::class.java))
            }
        }
    }
    private fun validUser(email: String, password: String) {
        if (email.isBlank() or password.isBlank()) return
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                if (authResult.user?.isEmailVerified == false) {
                    auth.signOut()
                    Toast.makeText(this, "Por favor, ative a conta através do link enviado no email e tente novamente!", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }
                startActivity(Intent(this, ClientMainScreenActivity::class.java))
            }.addOnFailureListener { exception ->
                if (exception.message.toString() == "The email address is badly formatted."){
                    Toast.makeText(this, "Endereço de email inválido, por favor digite novamente!", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, "Email ou senha incorretos, por favor digite novamente!", Toast.LENGTH_LONG).show()
                }
            }
    }
}