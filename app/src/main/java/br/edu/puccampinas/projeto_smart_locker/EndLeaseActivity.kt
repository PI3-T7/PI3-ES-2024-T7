package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityEndLeaseBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.*

class EndLeaseActivity : AppCompatActivity() {

    private val binding by lazy {ActivityEndLeaseBinding.inflate(layoutInflater)}
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Recebe os dados do Intent (passar uidLocacao, uidUnidade, horaLocacao e numeroArmario)
        val uidUnidade = intent.getStringExtra("uid_unidade")

        // Acessar o documento na coleção "Unidades de Locação" com base no uidUnidade
        if (uidUnidade != null) {
            db.collection("Unidades de Locação").document(uidUnidade).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Verificar se o documento existe e se contém o campo "prices" como um array
                        val prices = document.get("prices") as? ArrayList<*>
                        if (prices != null && prices.size >= 5) {
                            val valorDiaria: Double = try {
                                prices[4]?.toString()?.toDouble() ?: 0.0
                            } catch (e: NumberFormatException) {
                                0.0
                            }
                            val horaLocacao = intent.getStringExtra("hora_locacao")

                            // Obtenha a hora de término
                            val calendar = Calendar.getInstance()
                            val horaFim = calendar.time // Hora de término atual

                            // Converta as strings de hora em objetos Calendar
                            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

                            val horaInicio = Calendar.getInstance().apply {
                                time = horaLocacao?.let { formatoHora.parse(it) }!!
                            }
                            val horaFinish = Calendar.getInstance().apply {
                                time = formatoHora.parse(horaFim.toString())!!
                            }

                            // Calcule a diferença de tempo em minutos
                            val diferencaTempoMillis = horaFinish.timeInMillis - horaInicio.timeInMillis
                            val diferencaTempoMinutos = diferencaTempoMillis / (1000 * 60) // Conversão de milissegundos para minutos

                            // descobre valor por hora para descobrir valor por minuto de uso
                            val valorPorHora = valorDiaria/11.0 //11 é o tempo total da diária (7h-18h)
                            val valorPorMinuto = valorPorHora/60.0

                            // descobre quanto ficou a locação de acordo com o tempo de uso
                            val valorTempoUso = diferencaTempoMinutos * valorPorMinuto

                            // descobre o valor a ser estornado
                            val valorEstorno = valorDiaria - valorTempoUso

                            binding.tvDiaria.text = "Valor da diária: R$ $valorDiaria"
                            binding.tvUso.text = "Valor do tempo de uso: R$ $valorTempoUso"
                            binding.tvEstorno.text = "Valor estornado: R$ $valorEstorno"

                        } else {
                            // O array "prices" não tem pelo menos 5 elementos ou é nulo
                            Toast.makeText(this, "O array 'prices' não tem pelo menos 5 elementos ou é nulo", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // O documento não existe
                        Toast.makeText(this, "Documento não existe", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    // Tratar falha na leitura do documento
                    Toast.makeText(this, "Falha ao ler documento", Toast.LENGTH_SHORT).show()
                }

            // mudando o status no banco pois a locação foi encerrada
            val uidLocacao = intent.getStringExtra("uid_locacao")
            if (uidLocacao != null) {
                val locacoesRef = db.collection("Locações").document(uidLocacao)

                // Atualizar o status para false
                locacoesRef
                    .update("status", false)
            } else {
                Toast.makeText(this, "Uid da locação nulo", Toast.LENGTH_SHORT).show()
            }
        }

        // mudando o status no banco pois a locação foi encerrada (armario volta a ficar disponivel)
        val numeroArmario = intent.getStringExtra("numero_armario")

        if (uidUnidade != null && numeroArmario != null) {
            val unidadesRef = db.collection("Unidades de Locação").document(uidUnidade)

            // Atualizar o status do armário para true
            unidadesRef
                .get()
                .addOnSuccessListener { document ->
                    val lockers = document?.get("lockers") as? Map<String, Boolean>

                    if (lockers != null) {
                        // Atualizar o status do armário especificado para true
                        val updatedLockers = lockers.toMutableMap()
                        updatedLockers[numeroArmario] = true

                        // Atualizar o documento com o novo mapa de armários
                        unidadesRef
                            .update("lockers", updatedLockers)
                    } else {
                        // O mapa de armários não está disponível no documento
                        Toast.makeText(this, "Mapa de armários não encontrado no documento", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Tratar falha na leitura do documento
                    Toast.makeText(this, "Falha ao ler documento da unidade de locação: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Uid da unidade ou numero do armario nulos.", Toast.LENGTH_SHORT).show()
        }

        binding.btnConfirm.setOnClickListener {
            startActivity(Intent(this,ManagerMainScreenActivity::class.java))
            finish()
        }
    }
}