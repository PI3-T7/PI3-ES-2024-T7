package br.edu.puccampinas.projeto_smart_locker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityCardsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity para exibir e gerenciar cartões cadastrados.
 * @authors: Alex, Marcos e Isabella
 */
class CardsActivity : AppCompatActivity() {
    // Essa variavel broadcastReceiver vai possibilitar a realização de uma função nesta activity
    // a partir de outra, nesse caso a função de finalizar essa activity
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }
    // Vinculação da view usando o binding para inflar o layout
    private val binding by lazy { ActivityCardsBinding.inflate(layoutInflater) }
    // Instância do FirebaseAuth para autenticação
    private val auth by lazy { FirebaseAuth.getInstance() }
    // Instância do FirebaseFirestore para acesso ao banco de dados Firestore
    private val database by lazy { FirebaseFirestore.getInstance() }
    // RecyclerView para exibir os cartões
    private lateinit var recyclerView: RecyclerView
    // Lista mutável de cartões cadastrados
    private lateinit var itens: MutableList<CartoesCadastrados>
    // Adapter para gerenciar os itens da RecyclerView
    private lateinit var adapter: CardAdapter

    /**
     * @authors: Alex, Marcos e Isabella
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // Configuração do RecyclerView para exibir cartões em um layout de grade com 2 colunas
        recyclerView = binding.rvCards
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.setHasFixedSize(true)

        // Inicialização da lista de itens e do adapter
        itens = mutableListOf()
        adapter = CardAdapter(itens)

        // Configuração do adapter no RecyclerView
        recyclerView.adapter = adapter

        // Preenchimento dos dados a partir do Firestore
        preencherDados()

        // Aqui estão os botões de voltar e home do navbar
        binding.nav.buttonVoltar.setOnClickListener { finish() }
        binding.nav.buttonHome.setOnClickListener { finish() }

        // Aqui é atribuido um filtro ao broadcast para poder ser executado em outra activity a
        // partir deste filtro
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("meuFiltro"))
    }

    /**
     * Preenche os dados dos cartões a partir do Firestore.
     * @authors: Marcos e Isabella
     */
    private fun preencherDados() {
        database
            .collection("Pessoas")
            .document(auth.currentUser?.uid.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Erro no Firebase Firestore", error.message.toString())
                }
                if (snapshot != null && snapshot.exists()) {
                    val mapList = snapshot.get("cartoes") as? List<Map<String, Any>>
                    mapList?.forEach { map ->
                        if (map.containsKey("numero")) {
                            itens.add(CartoesCadastrados(map["numero"].toString()))
                        }
                    }
                    adapter.notifyDataSetChanged()

                }
            }
    }
}

/**
 * Classe que representa os cartões cadastrados.
 * @authors: Isabella e Marcos
 * @param numero O número do cartão.
 */
//      Cartão de crédito Visa  4.
//      Cartão de crédito Mastercard  5
//      Cartão de crédito American Express 3
//      Cartão de crédito Elo é iniciado pelo número 5 ou 6.
class CartoesCadastrados(numero: String) {
    val bandeira: String
    val numeroFormatado: String

    init {
        bandeira = determinarBandeira(numero)
        numeroFormatado = formatarNumero(numero)
    }

    /**
     * Determina a bandeira do cartão com base no número.
     * @author: Isabella
     * @param numeroCartao O número do cartão.
     * @return A bandeira do cartão.
     */
    private fun determinarBandeira(numeroCartao: String): String {
        return when {
            numeroCartao.startsWith("3") -> "A. Express"
            numeroCartao.startsWith("4") -> "Visa"
            numeroCartao.startsWith("5") -> "Mastercard"
            numeroCartao.startsWith("6") -> "Elo"
            else -> "Desconhecida"
        }
    }
    /**
     * Formata o número do cartão para exibição.
     * @author: Isabella
     * @param numeroCartao O número do cartão.
     * @return O número do cartão formatado.
     */
    // formatarNumero: faz a formatação do número do cartão para exibir dessa forma : "**** 1234"
    private fun formatarNumero(numeroCartao: String): String {
        val primeiraParte = "**"
        val ultimosDigitos = numeroCartao.takeLast(4)
        return "$primeiraParte $ultimosDigitos"
    }
}