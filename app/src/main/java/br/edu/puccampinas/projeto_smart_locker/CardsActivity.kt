package br.edu.puccampinas.projeto_smart_locker

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityCardsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CardsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCardsBinding.inflate( layoutInflater ) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance() }
    private lateinit var recyclerView: RecyclerView
    private lateinit var itens: MutableList<CartoesCadastrados>
    private lateinit var adapter: CardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        recyclerView = binding.rvCards
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.setHasFixedSize(true)

        itens = mutableListOf()

        adapter = CardAdapter(itens)

        // Chamando o Adapter e seu gerenciador de Layouts
        recyclerView.adapter = adapter

        preencherDados()

        // obs para marcos
        // como usei uma nav bar na tela de cartao, pra acessar os icones home e voltar tem que usar
        // o binding.nav.buttonVoltar e binding.nav.buttonHome
    }

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
