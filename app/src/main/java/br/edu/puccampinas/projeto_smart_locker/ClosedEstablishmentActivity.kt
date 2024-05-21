package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityClosedEstablishmentBinding

class ClosedEstablishmentActivity : AppCompatActivity() {

    private val binding by lazy { ActivityClosedEstablishmentBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnMenu.setOnClickListener {
            startActivity(Intent(this, ClientMainScreenActivity::class.java))
        }

    }
}