package br.edu.puccampinas.projeto_smart_locker

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityRentedLockerBinding
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity responsável por exibir os detalhes de uma locação concluida
 */
class RentedLockerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityRentedLockerBinding.inflate(layoutInflater) }
    private var locacaoID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Recupera os dados passados da atividade anterior
        val dados = intent.getStringExtra("dadosCliente")
        locacaoID = intent.getStringExtra("uid_locacao")

        // Verifica se os dados são nulos e chama a função
        if (dados != null && locacaoID != null) {
            updateUIWithDocumentData(dados, locacaoID!!)
        } else {
            Log.e("RentedLockerActivity", "Dados do cliente ou ID da locação são nulos")
        }
    }

    /**
     * Atualiza a UI com os dados do documento.
     *
     * @param dadosJson String contendo os dados do cliente em formato JSON.
     * @param locacaoId ID da locação.
     */
    private fun updateUIWithDocumentData(dadosJson: String, locacaoId: String) {
        val jsonObject = JSONObject(dadosJson)
        val unidadeId = jsonObject.getString("unidade")
        val tempo = jsonObject.getString("opcao")
        val preco = jsonObject.getDouble("preco")

        val db = FirebaseFirestore.getInstance()

        // Formata o preço para exibição
        val precoFormatado = String.format("R$ %.2f", preco).replace('.', ',')
        binding.tvPrice.text = precoFormatado
        Log.d("RentedLockerActivity", "Preço formatado: $precoFormatado")

        // Recupera os dados da unidade do Firestore
        db.collection("Unidades de Locação")
            .document(unidadeId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val unidadeNome = document.getString("name")
                    val endereco = document.getString("address")

                    binding.tvUnit.text = unidadeNome
                    binding.tvAddress.text = endereco
                    Log.d(
                        "RentedLockerActivity",
                        "Dados da unidade: Nome - $unidadeNome, Endereço - $endereco"
                    )
                } else {
                    Log.e("RentedLockerActivity", "Documento da unidade não existe")
                }
            }
            .addOnFailureListener { e ->
                Log.e("RentedLockerActivity", "Erro ao obter dados da unidade: ${e.message}")
            }

        // Recupera os dados da locação do Firestore
        db.collection("Locações")
            .document(locacaoId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val data = document.getString("data_locacao")
                    val hora = document.getString("hora_locacao")

                    // Formatação do inicio e fim da locação
                    if (!data.isNullOrEmpty() && !hora.isNullOrEmpty()) {
                        val dateTimeFormat =
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val startDate = dateTimeFormat.parse("$data $hora")
                        if (startDate != null) {
                            val tempoEscolhido = tempo.replace("\\D".toRegex(), "").toInt()

                            val calendar = Calendar.getInstance().apply {
                                time = startDate
                                add(Calendar.HOUR_OF_DAY, tempoEscolhido)
                            }
                            val endDate = calendar.time
                            val endDateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                            val endDateString = endDateFormat.format(endDate)

                            binding.tvStart.text = "Início: ${dateTimeFormat.format(startDate)}"
                            binding.tvFinish.text = "Fim: $endDateString"
                            binding.tvTime.text = "${tempoEscolhido} hora(s)"

                            Log.d(
                                "RentedLockerActivity",
                                "Início: ${dateTimeFormat.format(startDate)}, Fim: $endDateString, Tempo: $tempoEscolhido horas"
                            )
                        } else {
                            binding.tvFinish.text = "Fim: Não foi possível calcular"
                            Log.e(
                                "RentedLockerActivity",
                                "Não foi possível calcular a data de término"
                            )
                        }
                    } else {
                        binding.tvStart.text = "Início: Dados inválidos"
                        binding.tvFinish.text = "Fim: Dados inválidos"
                        Log.e("RentedLockerActivity", "Dados de data ou hora inválidos")
                    }
                } else {
                    Log.e("RentedLockerActivity", "Documento da locação não existe")
                }
            }
            .addOnFailureListener { e ->
                Log.e("RentedLockerActivity", "Erro ao obter dados da locação: ${e.message}")
            }
    }
}
