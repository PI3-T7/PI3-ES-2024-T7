package br.edu.puccampinas.projeto_smart_locker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityCartoesBinding

class CartoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartoesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartoesBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val itens = listOf(
            CartoesCadastrados("VISA","**** 5256"),
            CartoesCadastrados("VISA","**** 5552"),
            CartoesCadastrados("MASTERCARD","**** 4115"),
            CartoesCadastrados("VISA","**** 8154"),
        )

        binding.rvCards.adapter = CardAdapter(itens)
        binding.rvCards.layoutManager = GridLayoutManager(
            this,
            2
        )

    }
}

data class CartoesCadastrados(
    val bandeira: String,
    val numero: String
)