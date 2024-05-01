package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityForgetBinding
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityManagerMainScreenBinding

class ManagerMainScreenActivity : AppCompatActivity() {

    private val binding by lazy { ActivityManagerMainScreenBinding.inflate( layoutInflater ) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.viewReadNfc.setOnClickListener{
            startActivity(Intent(this,ReadNnfActivity::class.java))
        }
    }
}