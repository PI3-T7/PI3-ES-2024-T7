package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivitySelectPeopleBinding

class SelectPeopleNumActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySelectPeopleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectPeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonConfirm.setOnClickListener {
            if (binding.button1person.isChecked) {
                // Se o botão abrir armario estiver selecionado
                val intent = Intent(this, TakePicActivity::class.java)
                startActivity(intent)
            } else if (binding.button2persons.isChecked) {
                // Se o botão encerrar locação estiver selecionado
                val intent = Intent(this, TakePicActivity::class.java)
                startActivity(intent)
            } else {
                // Se nenhum botão estiver selecionado
                Toast.makeText(this, "Por favor, selecione uma opção", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonHome2.setOnClickListener {
            startActivity(Intent(this,ManagerMainScreenActivity::class.java))
            finish()
        }

        binding.buttonVoltar2.setOnClickListener {
            startActivity(Intent(this,QRcodeManagerActivity::class.java))
            finish()
        }
    }
}
