package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityEndLeaseBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Classe responsável pela atividade de encerramento de locação.
 * @author: Lais
 */
class EndLeaseActivity : AppCompatActivity() {

    // Binding da atividade
    private val binding by lazy { ActivityEndLeaseBinding.inflate(layoutInflater) }

    // Referência ao Firestore
    private val db = FirebaseFirestore.getInstance()

    /**
     * Método chamado quando a atividade é criada.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configurar o botão de confirmação para voltar à tela principal do gerente
        binding.btnConfirm.setOnClickListener {
            startActivity(Intent(this, ManagerMainScreenActivity::class.java))
            finish()
        }

        // Obter o ID da locação passado através da intent
        val idLocacao = intent.getStringExtra("idLocacao")

        // Verifique se o ID da locação não é nulo
        idLocacao?.let { id ->
            // Referência para o documento específico da locação
            val locacaoRef = db.collection("Locações").document(id)

            // Obtenha os dados do documento da locação
            locacaoRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Documento existe, obtenha os campos desejados
                        val uidUnidade = document.getString("uid_unidade")
                        val horaLocacao = document.getString("hora_locacao")
                        val numeroArmario = document.getString("numero_armario")
                        Log.d(TAG, "Hora da Locação: $horaLocacao")

                        // Criar uma coroutine
                        GlobalScope.launch(Dispatchers.Main) {
                            // Chamar a função suspensa dentro da coroutine
                            val valorDiaria: Double? = uidUnidade?.let { obterDiaria(it) }
                            Log.d(TAG, "Valor da diária: $valorDiaria")

                            // Obtenha a hora de término
                            val calendar = Calendar.getInstance()
                            val horaFim = calendar.time // Hora de término atual
                            val horarioFormatado = obterHorarioFormatado(horaFim)
                            Log.d(TAG, "Horario Fim Locação: $horarioFormatado")

                            // Formatar horaLocacao e horaFim para Date
                            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val formatoCompleto =
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                            // Obtenha a data atual no formato de data e hora completa
                            val dataAtual =
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                            // Formatar horaLocacao e horaFim para Date
                            val horaLocacaoDate = formatoCompleto.parse("$dataAtual $horaLocacao")
                            var horaFimDate = formatoCompleto.parse("$dataAtual $horarioFormatado")

                            // Se a hora de término for anterior à hora de locação, adicione um dia
                            if (horaFimDate.before(horaLocacaoDate)) {
                                val cal = Calendar.getInstance()
                                cal.time = horaFimDate
                                cal.add(Calendar.DAY_OF_MONTH, 1)
                                horaFimDate = cal.time
                            }

                            // Calcule a diferença de tempo em minutos
                            val diferencaTempoMillis = horaFimDate.time - horaLocacaoDate.time
                            val diferencaTempoMinutos =
                                diferencaTempoMillis / (1000 * 60) // Conversão de milissegundos para minutos
                            Log.d(TAG, "Diferenca Tempo Minutos: $diferencaTempoMinutos")

                            // Descobre valor por hora para descobrir valor por minuto de uso
                            val valorPorHora =
                                valorDiaria?.div(11.0) // 11 é o tempo total da diária (7h-18h)
                            val valorPorMinuto = valorPorHora?.div(60.0)

                            // Descobre quanto ficou a locação de acordo com o tempo de uso
                            val valorTempoUso = valorPorMinuto?.let {
                                val valor = diferencaTempoMinutos * it
                                BigDecimal(valor).setScale(2, RoundingMode.HALF_UP).toDouble()
                            }

                            // Descobre o valor a ser estornado
                            val valorEstorno = valorDiaria?.let { it - (valorTempoUso ?: 0.0) }

                            // Criar um formatador para moeda brasileira
                            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

                            // Formatar os valores
                            val valorDiariaFormatado =
                                formatador.format(valorDiaria).replace("R$", "").trim()
                            val valorTempoUsoFormatado =
                                formatador.format(valorTempoUso).replace("R$", "").trim()
                            val valorEstornoFormatado =
                                formatador.format(valorEstorno).replace("R$", "").trim()

                            // Definir os valores formatados no TextView
                            binding.tvDiaria.text = "R$ $valorDiariaFormatado"
                            binding.tvUso.text = "R$ $valorTempoUsoFormatado"
                            binding.tvEstorno.text = "R$ $valorEstornoFormatado"

                            // Atualize o status da locação
                            atualizarStatusLocacao(id, false)

                            // Atualize o status do armário
                            if (uidUnidade != null && numeroArmario != null) {
                                atualizarStatusArmario(uidUnidade, numeroArmario)
                            }
                        }
                    } else {
                        // Documento não existe ou está vazio
                        Log.d(TAG, "Documento não encontrado ou vazio")
                    }
                }
                .addOnFailureListener { exception ->
                    // Tratar falha na obtenção dos dados
                    Log.e(TAG, "Erro ao obter documento:", exception)
                }
        } ?: run {
            // ID da locação é nulo
            Log.d(TAG, "ID da locação é nulo")
        }
    }

    companion object {
        private const val TAG = "EndLeaseActivity"
    }

    /*
    * Método para obter a diária da unidade de locação.
    */
    private suspend fun obterDiaria(uidUnidade: String): Double {
        return withContext(Dispatchers.IO) {
            // Referência para o documento específico da unidade de locação
            val unidadeRef = db.collection("Unidades de Locação").document(uidUnidade)

            try {
                // Obtenha o documento de forma assíncrona
                val document = unidadeRef.get().await()

                if (document.exists()) {
                    // Documento existe, obtenha o array de preços
                    val prices = document.get("prices") as? List<Double>

                    // Verifique se o array de preços não é nulo e tem pelo menos 5 elementos
                    if (prices != null && prices.size >= 5) {
                        // Obtenha o quinto elemento (índice 4) do array de preços
                        val diaria = prices[4]

                        // Retorne o valor da diária
                        diaria
                    } else {
                        // O array de preços é nulo ou não tem pelo menos 5 elementos
                        throw IllegalStateException("Array de preços é nulo ou tem menos de 5 elementos")
                    }
                } else {
                    // Documento não existe ou está vazio
                    throw IllegalStateException("Documento não encontrado ou vazio")
                }
            } catch (e: Exception) {
                // Tratar falha na obtenção dos dados
                throw e
            }
        }
    }

    /*
     * Método para formatar a hora.
     */
    private fun obterHorarioFormatado(date: Date): String {
        val formato = SimpleDateFormat("HH:mm", Locale.getDefault()) // Define o formato de hora
        return formato.format(date) // Formata a data para o formato especificado
    }

    /*
     * Método para atualizar o status da locação no banco de dados.
     */
    private fun atualizarStatusLocacao(uidLocacao: String, status: Boolean) {
        val locacoesRef = db.collection("Locações").document(uidLocacao)

        locacoesRef
            .update("status", status)
            .addOnSuccessListener {
                Log.d(TAG, "Status da locação atualizado com sucesso")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao atualizar status da locação", e)
            }
    }

    /*
     * Método para atualizar o status do armário no banco de dados.
     */
    private fun atualizarStatusArmario(uidUnidade: String, numeroArmario: String) {
        val unidadesRef = db.collection("Unidades de Locação").document(uidUnidade)

        unidadesRef.get()
            .addOnSuccessListener { document ->
                val lockers = document?.get("lockers") as? Map<String, Boolean>

                if (lockers != null) {
                    // Atualizar o status do armário especificado para true
                    val updatedLockers = lockers.toMutableMap()
                    updatedLockers[numeroArmario] = true

                    // Atualizar o documento com o novo mapa de armários
                    unidadesRef
                        .update("lockers", updatedLockers)
                        .addOnSuccessListener {
                            Log.d(TAG, "Status do armário atualizado com sucesso")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Erro ao atualizar status do armário", e)
                        }
                } else {
                    // O mapa de armários não está disponível no documento
                    Toast.makeText(
                        this,
                        "Mapa de armários não encontrado no documento",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                // Tratar falha na leitura do documento
                Log.e(TAG, "Falha ao ler documento da unidade de locação", e)
            }
    }
}