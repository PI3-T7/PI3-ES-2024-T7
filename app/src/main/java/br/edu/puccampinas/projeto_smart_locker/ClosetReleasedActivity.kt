package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityClosetReleasedBinding

class ClosetReleasedActivity: AppCompatActivity() {

    private val binding by lazy {ActivityClosetReleasedBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonHome2.setOnClickListener{
            startActivity(Intent(this,ManagerMainScreenActivity::class.java))
            finish()
        }
    }
}