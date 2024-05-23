package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityWaitingBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.json.JSONException
import org.json.JSONObject

/**
 * Activity responsável por monitorar o estado da locação enquanto espera a conclusão.
 */
class WaitingActivity : AppCompatActivity() {

    private val binding by lazy { ActivityWaitingBinding.inflate(layoutInflater) }

    // Ouvinte para monitorar as alterações nos documentos do Firestore
    private var firestoreListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.lottieAnimationView.playAnimation()

        val dados = intent.getStringExtra("dadosCliente")

        // Verifica se os dados são nulos e chama a função
        if (dados != null) {
            monitorarLocacao(dados)
        } else {
            Log.e("WaitingActivity", "Dados recebidos são nulos")
        }
    }

    /**
     * Monitora o estado da locação com base nos dados do cliente.
     *
     * @param dadosJson String contendo os dados do cliente em formato JSON.
     */
    private fun monitorarLocacao(dadosJson: String) {
        try {
            // Extrai os dados do JSON
            val jsonObject = JSONObject(dadosJson)
            val clienteId = jsonObject.getString("id")
            val unidade = jsonObject.getString("unidade")

            // Inicializa o Firestore
            val db = FirebaseFirestore.getInstance()
            firestoreListener = db.collection("Pendencias")
                .whereEqualTo("uid_cliente", clienteId)
                .whereEqualTo("uid_unidade", unidade)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("WaitingActivity", "Erro ao monitorar locação: ${e.message}")
                        Toast.makeText(
                            this,
                            "Erro ao monitorar locação: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addSnapshotListener
                    }
                    if (snapshots != null && !snapshots.isEmpty) {
                        // Verifica se há documentos de pendência e processa-os
                        for (doc in snapshots.documents) {
                            val status = doc.getString("status")
                            val uidLocacao = doc.getString("uid_locacao")  // Pega o ID da locação
                            Log.d("WaitingActivity", "Status atual: $status")
                            if (status == "concluido" && uidLocacao != null) {
                                // Redireciona para a tela de finalização
                                Log.d(
                                    "WaitingActivity",
                                    "Status concluido encontrado, redirecionando..."
                                )
                                val intent = Intent(this, RentedLockerActivity::class.java)
                                intent.putExtra(
                                    "uid_locacao",
                                    uidLocacao
                                )  // Passa o ID da locação para a próxima activity
                                intent.putExtra(
                                    "dadosCliente",
                                    dadosJson
                                )  // Passa os dados do cliente para a próxima activity
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        Log.d("WaitingActivity", "Nenhum documento encontrado.")
                    }
                }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao processar os dados do QR code", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreListener?.remove()
    }
}
