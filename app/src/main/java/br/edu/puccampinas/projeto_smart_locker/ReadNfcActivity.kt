package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityReadNfcBinding

class ReadNfcActivity : AppCompatActivity() {

    private val binding by lazy { ActivityReadNfcBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.imgArrow.setOnClickListener {
            finish()
        }

        // dependendo de como for feita a leitura da pulseira nfc deletamos o botao do xml
        // vou deixar por enquanto para poder navegar entre as paginas do gerente
        binding.btnAbrirArmario.setOnClickListener {
            startActivity(Intent(this,CheckDataNfcActivity::class.java))
        }
    }
}