package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivitySelectPeopleBinding

class SelectPeopleNumActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySelectPeopleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectPeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonConfirm.setOnClickListener {
            val intent = Intent(this, TakePicActivity::class.java)
            startActivity(intent)
        }
    }
}
