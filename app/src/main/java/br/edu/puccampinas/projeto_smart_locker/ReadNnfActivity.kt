package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityManagerMainScreenBinding
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityReadNnfBinding

class ReadNnfActivity : AppCompatActivity() {

    private val binding by lazy { ActivityReadNnfBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.imgArrow.setOnClickListener {
            startActivity(Intent(this,ManagerMainScreenActivity::class.java))
            finish()
        }

        binding.btnAbrirArmario.setOnClickListener {
            //startActivity(Intent(this,ManagerMainScreenActivity::class.java))
        }
    }

    // dependendo de como for feita a leitura da pulseira nfc deletamos o botao do xml
    // vou deixar por enquanto para poder navegar entre as paginas do gerente
}