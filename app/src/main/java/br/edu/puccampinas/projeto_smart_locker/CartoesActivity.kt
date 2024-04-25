package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityCartoesBinding

class CartoesActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCartoesBinding.inflate( layoutInflater ) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            buttonVoltar.setOnClickListener { finish() }
            buttonHome.setOnClickListener { finish() }
            buttonCadastrar.setOnClickListener {
                startActivity(Intent(this@CartoesActivity, CartaoCadastrandoActivity::class.java))
            }
        }
    }
}

