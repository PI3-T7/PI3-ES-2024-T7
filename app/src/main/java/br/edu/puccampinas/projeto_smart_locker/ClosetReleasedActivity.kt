package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityClosetReleasedBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity para exibir os detalhes da locação e fotos relacionadas.
 */
class ClosetReleasedActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var dadosCliente: DadosCliente
    private val binding by lazy { ActivityClosetReleasedBinding.inflate(layoutInflater) }

    private var totalFotos = 0 // Contador total de fotos a serem carregadas
    private var fotosCarregadas = 0 // Contador de fotos já carregadas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            buttonHome2.setOnClickListener {
                startActivity(
                    Intent(
                        this@ClosetReleasedActivity,
                        ManagerMainScreenActivity::class.java
                    )
                )
                finish()
            }

            // Configura os botões de navegação entre as fotos
            prevButton.setOnClickListener {
                binding.viewFlipper.showPrevious()
                updatePhotoIndicator()
            }
            nextButton.setOnClickListener {
                binding.viewFlipper.showNext()
                updatePhotoIndicator()
            }
        }

        /**
         * *****APAGAR COMENTARIO*******
         * Isabella
         * Obs: Comentei essa parte para não dar erro já que estou testando apenas puxar a foto do banco,
         * então não está vindo nada do intent nesse momento
         */
        // Obtém os dados do cliente da Intent
        //val dadosJson = intent.getStringExtra("dadosCliente")
        //dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)

        // Carrega os dados do usuário utilizando o ID do cliente
        //loadUserData(dadosCliente.id)

        val locacaoId =
            "8AzYdy1i9CTFa1rxQVF5" // ID do documento de teste
        loadLocacaoData(locacaoId)

    }

    /**
     * Carrega os dados da locação a partir do Firestore.
     *
     * @param locacaoId O ID do documento da locação a ser carregada.
     */
    private fun loadLocacaoData(locacaoId: String) {
        showProgressBar()
        // Usando coroutines para lidar com operações assíncronas
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Obtém o documento da locação do Firestore
                val document = db.collection("Locações").document(locacaoId).get().await()
                if (document.exists()) {
                    // Atualiza a UI com os dados do documento
                    updateUIWithDocumentData(document)
                    val fotos = document.get("fotos") as? List<*>
                    if (!fotos.isNullOrEmpty()) {
                        totalFotos = fotos.size
                        // Adiciona cada imagem ao ViewFlipper
                        fotos.forEach { fotoUrl ->
                            if (fotoUrl is String) {
                                addImageToFlipper(fotoUrl)
                            }
                        }
                        // Atualiza o indicador de fotos
                        updatePhotoIndicator()
                    } else {
                        showError("Fotos não encontradas")
                    }
                } else {
                    showError("Documento não encontrado")
                }
            } catch (e: Exception) {
                showError("Erro ao carregar dados: ${e.message}")
                Log.e("MinhaTag", "Erro ao carregar dados", e)
            }
        }
    }

    /**
     * Atualiza a interface do usuário com os dados do documento.
     *
     * @param document O documento do Firestore contendo os dados da locação.
     */
    private fun updateUIWithDocumentData(document: DocumentSnapshot) {

        /**
         *****APAGAR COMENTARIO*****
         * Isabella
         * Como comentei o intent do qrcode alguns dados estão estáticos
         * Os dados que estou puxando do banco é de um documento Locação criado para teste de id PQyQaoogCooC4twNk262
         */

        // Atualiza os elementos da UI com os dados do documento
        with(binding) {
            tvUnit.text = "Terminal Rodoviário de Campinas"
            tvAddress.text = "R. Dr. Pereira Lima, 85 - Vila Industrial, Campinas - SP, 13035-505"
            tvName.text = "Isabella Maria T Bruno"
            tvPhone.text = "(19) 98913 3737"
            tvNumber.text = "Armário: ${document.getString("numero_armario")}"
            tvStartLocation.text =
                "Início: ${document.getString("data_locacao")} ${document.getString("hora_locacao")}"

            val dateLocacao = document.getString("data_locacao")
            val horaLocacao = document.getString("hora_locacao")
            val tempoEscolhidoString = document.getString("tempo_escolhido") // Obtém a string do Firestore
            val tempoEscolhido: Int =
                // Extrai apenas os caracteres numéricos da string e converte para inteiro
                tempoEscolhidoString?.replace("\\D".toRegex(), "")?.toInt()
                    ?: // Define um valor padrão (0) se a string for nula
                    0

            val dateTimeFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            val startDate = dateTimeFormat.parse("$dateLocacao $horaLocacao")
            if (startDate != null) {
                val calendar = Calendar.getInstance().apply {
                    time = startDate
                    add(Calendar.HOUR_OF_DAY, tempoEscolhido)
                }
                val endDate = calendar.time
                val endDateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                val endDateString = endDateFormat.format(endDate)
                tvEndLocation.text = "Fim: $endDateString"
            } else {
                tvEndLocation.text = "Fim: Não foi possível calcular"
            }

            tvTime.text = "${tempoEscolhido} hora(s)"
            val preco = document.getLong("preco")?.toDouble() ?: 0.0
            val precoFormatado = String.format("R$ %.2f", preco).replace('.', ',')
            tvPrice.text = precoFormatado
        }
    }

    /**
     * Adiciona uma imagem ao ViewFlipper e carrega a imagem a partir do URL do Firebase Storage.
     *
     * @param fotoUrl O URL da foto a ser carregada.
     */
    private fun addImageToFlipper(fotoUrl: String) {
        // Cria uma ImageView para exibir a imagem
        val imageview = ImageView(this).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        // Usando coroutines para lidar com operações assíncronas
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Obtém o URI da imagem no Firebase Storage
                val uri = storage.getReferenceFromUrl(fotoUrl).downloadUrl.await()
                // Carrega a imagem na ImageView usando Glide
                Glide.with(this@ClosetReleasedActivity)
                    .load(uri)
                    .into(imageview)

                fotosCarregadas++
                // Esconde a barra de progresso quando todas as fotos forem carregadas
                if (fotosCarregadas == totalFotos) {
                    hideProgressBar()
                }
            } catch (e: Exception) {
                // Exibe uma mensagem de erro se houver falha ao obter a URL da imagem
                showError("Erro ao obter URL da foto: ${e.message}")
                Log.e("MinhaTag", "Erro ao obter URL da foto", e)

                fotosCarregadas++
                // Esconde a barra de progresso quando todas as fotos forem carregadas (mesmo em caso de erro)
                if (fotosCarregadas == totalFotos) {
                    hideProgressBar()
                }
            }
        }

        // Adiciona a ImageView ao ViewFlipper
        binding.viewFlipper.addView(imageview)
    }

    /**
     * Atualiza o indicador de fotos para mostrar a foto atual e o total de fotos.
     */
    private fun updatePhotoIndicator() {
        val displayedChild = binding.viewFlipper.displayedChild
        val totalChildren = binding.viewFlipper.childCount
        binding.photoIndicator.text = "${displayedChild + 1}/$totalChildren"

        binding.prevButton.isEnabled = displayedChild != 0
        binding.nextButton.isEnabled = displayedChild != totalChildren - 1
    }

    /**
     * Mostra a barra de progresso e esconde o conteúdo principal.
     */
    private fun showProgressBar() {
        binding.loading.visibility = View.VISIBLE
        binding.main.visibility = View.GONE
    }

    /**
     * Esconde a barra de progresso e mostra o conteúdo principal.
     */
    private fun hideProgressBar() {
        binding.loading.visibility = View.GONE
        binding.main.visibility = View.VISIBLE
    }

    /**
     * Exibe uma mensagem de erro usando um Toast e esconde a barra de progresso.
     *
     * @param message A mensagem de erro a ser exibida.
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        hideProgressBar()
    }
}
