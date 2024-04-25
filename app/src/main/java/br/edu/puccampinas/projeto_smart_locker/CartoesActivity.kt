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

        // Lista para adicionar os itens no recyclerview
        // ainda está estática, tem que puxar do banco
        val itens = mutableListOf(
            CartoesCadastrados("5896 5895 7841 5589"),
            CartoesCadastrados("3562 6785 2565 2565"),
            CartoesCadastrados("6589 6785 2565 2565"),
        )

        // Chamando o Adapter e seu gerenciador de Layouts
        //binding.rvCards.adapter = CardAdapter(itens)
        binding.rvCards.layoutManager = GridLayoutManager(
            this,
            2
        )


        binding = ActivityCartoesBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Lista para adicionar os itens no recyclerview
        // ainda está estática, tem que puxar do banco
        val itens = mutableListOf(
            CartoesCadastrados("5896 5895 7841 5589"),
            CartoesCadastrados("3562 6785 2565 2565"),
            CartoesCadastrados("6589 6785 2565 2565"),
            )

        // Chamando o Adapter e seu gerenciador de Layouts
//        binding.rvCards.adapter = CardAdapter(itens)
//        binding.rvCards.layoutManager = GridLayoutManager(
//            this,
//            2
//        )

    }
}


//      Cartão de crédito Visa  4.
//      Cartão de crédito Mastercard  5
//      Cartão de crédito American Express 3
//      Cartão de crédito Elo é iniciado pelo número 5 ou 6.

// Classe dos cartoes cadastrados
class CartoesCadastrados(numero: String) {
    val bandeira: String
    val numeroFormatado: String

    init {
        bandeira = determinarBandeira(numero)
        numeroFormatado = formatarNumero(numero)
    }

    // Função para determinar qual a bandeira do cartão
    private fun determinarBandeira(numeroCartao: String): String {
        return when {
            numeroCartao.startsWith("3") -> "A. Express"
            numeroCartao.startsWith("4") -> "Visa"
            numeroCartao.startsWith("5") -> "Mastercard"
            numeroCartao.startsWith("6") -> "Elo"
            else -> "Desconhecida"
        }
    }

    private fun formatarNumero(numeroCartao: String): String {
        val primeiraParte = "****"
        val ultimosDigitos = numeroCartao.takeLast(4)
        return "$primeiraParte $ultimosDigitos"
    }
}
//      Cartão de crédito Visa  4.
//      Cartão de crédito Mastercard  5
//      Cartão de crédito American Express 3
//      Cartão de crédito Elo é iniciado pelo número 5 ou 6.

// Classe dos cartoes cadastrados
class CartoesCadastrados(numero: String) {
    val bandeira: String
    val numeroFormatado: String

    init {
        bandeira = determinarBandeira(numero)
        numeroFormatado = formatarNumero(numero)
    }

    // Função para determinar qual a bandeira do cartão
    private fun determinarBandeira(numeroCartao: String): String {
        return when {
            numeroCartao.startsWith("3") -> "A. Express"
            numeroCartao.startsWith("4") -> "Visa"
            numeroCartao.startsWith("5") -> "Mastercard"
            numeroCartao.startsWith("6") -> "Elo"
            else -> "Desconhecida"
        }
    }

    private fun formatarNumero(numeroCartao: String): String {
        val primeiraParte = ""
        val ultimosDigitos = numeroCartao.takeLast(4)
        return "$primeiraParte $ultimosDigitos"
    }
}