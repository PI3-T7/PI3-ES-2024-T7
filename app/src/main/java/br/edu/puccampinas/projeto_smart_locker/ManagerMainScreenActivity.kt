package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityManagerMainScreenBinding

class ManagerMainScreenActivity : AppCompatActivity() {

    private val binding by lazy { ActivityManagerMainScreenBinding.inflate( layoutInflater ) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.viewReadNfc.setOnClickListener{
            startActivity(Intent(this,ReadNfcActivity::class.java))
        }

        binding.viewReleaseRental.setOnClickListener{
            startActivity(Intent(this,QRcodeManagerActivity::class.java))
        }
    }
}