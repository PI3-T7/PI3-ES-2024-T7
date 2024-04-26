package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityVerifyBinding
import com.google.firebase.auth.FirebaseAuth

class VerifyActivity : AppCompatActivity() {
    // Configuração do ViewBinding
    private val binding by lazy { ActivityVerifyBinding.inflate( layoutInflater ) }
    // Configuração do FirebaseAuth
    private val auth by lazy { FirebaseAuth.getInstance() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding){
            emailText.text = auth.currentUser?.email
            auth.signOut()
            butOk.setOnClickListener {
                startActivity(Intent(this@VerifyActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}