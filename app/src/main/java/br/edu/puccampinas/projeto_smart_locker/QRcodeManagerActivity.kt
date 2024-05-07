package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityQrcodeManagerBinding

class QRcodeManagerActivity: AppCompatActivity() {

    private val binding by lazy { ActivityQrcodeManagerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.imgArrow.setOnClickListener {
            startActivity(Intent(this,ManagerMainScreenActivity::class.java))
        }

        binding.btnExcluir.setOnClickListener {
            startActivity(Intent(this,SelectPeopleNumActivity::class.java))
        }

    }
}