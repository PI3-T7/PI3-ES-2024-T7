package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityTakePicBinding

class TakePicActivity: AppCompatActivity() {

    private lateinit var binding: ActivityTakePicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Listener para seta de voltar, redirecionando o usuário a tela anterior
        binding.imgArrow.setOnClickListener {
            val intent = Intent(this, SelectPeopleNumActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.buttonTakePic.setOnClickListener {
            startActivity(Intent(this,PersonPicActivity::class.java))
        }
    }
}
