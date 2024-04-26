package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityForgetBinding

class ForgetActivity : AppCompatActivity() {
    private val binding by lazy { ActivityForgetBinding.inflate( layoutInflater ) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.appCompatTextView1.text = intent.extras?.getString("email")
        binding.butOk.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}