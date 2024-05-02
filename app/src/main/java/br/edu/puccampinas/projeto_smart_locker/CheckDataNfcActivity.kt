package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityCheckDataNfcBinding

class CheckDataNfcActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCheckDataNfcBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.imgArrow.setOnClickListener {
            finish()
        }

        binding.btnConfirm.setOnClickListener {
            startActivity(Intent(this,OpenLockerActivity::class.java))
        }
    }
}