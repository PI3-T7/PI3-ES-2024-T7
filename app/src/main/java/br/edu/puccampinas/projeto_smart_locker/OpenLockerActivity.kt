package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityOpenLockerBinding

class OpenLockerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityOpenLockerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.imgArrow.setOnClickListener {
            finish()
        }

        binding.btConfirm.setOnClickListener {
            if (binding.btnOpen.isChecked) {
                // Se o botão abrir armario estiver selecionado
                val intent = Intent(this, ConfirmOpenActivity::class.java)
                startActivity(intent)
            } else if (binding.btnFinish.isChecked) {
                // Se o botão encerrar locação estiver selecionado
                val intent = Intent(this, EndLeaseActivity::class.java)
                startActivity(intent)
            } else {
                // Se nenhum botão estiver selecionado
                Toast.makeText(this, "Por favor, selecione uma opção", Toast.LENGTH_SHORT).show()
            }
        }
    }
}