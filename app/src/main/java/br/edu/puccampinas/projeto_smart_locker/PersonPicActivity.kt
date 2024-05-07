package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityPersonPicBinding

class PersonPicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonPicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonPicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Listener para o botão de finalizar, redirecionando para a tela seguinte
        binding.buttonFinish.setOnClickListener {
            val intent = Intent(this, ClosetReleasedActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Listener para seta de voltar, redirecionando o usuário a tela anterior
        binding.imgArrow.setOnClickListener {
            val intent = Intent(this, TakePicActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
