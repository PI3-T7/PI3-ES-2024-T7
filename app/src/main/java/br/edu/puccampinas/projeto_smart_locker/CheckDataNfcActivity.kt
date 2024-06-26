package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityCheckDataNfcBinding
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
 * Activity que exibe os dados de uma locação para que o gerente possa confirmar se os dados batem
 * @author Isabella
 */
class CheckDataNfcActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCheckDataNfcBinding.inflate(layoutInflater) }
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var totalFotos = 0 // Contador total de fotos a serem carregadas
    private var fotosCarregadas = 0 // Contador de fotos já carregadas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val idLocacao = intent.getStringExtra("tag_data")
        if (idLocacao != null) {
            loadLocacaoData(idLocacao)
        }

        with(binding) {
            // Configura os botões de navegação entre as fotos
            prevButton.setOnClickListener {
                binding.viewFlipper.showPrevious()
                updatePhotoIndicator()
            }
            nextButton.setOnClickListener {
                binding.viewFlipper.showNext()
                updatePhotoIndicator()
            }
            imgArrow.setOnClickListener { finish() }
            // A foto corresponde à pessoa
            btnConfirm.setOnClickListener {
                val intent = Intent(this@CheckDataNfcActivity,OpenLockerActivity::class.java)
                intent.putExtra("idLocacao", idLocacao)
                LocalBroadcastManager.getInstance(this@CheckDataNfcActivity).sendBroadcast(Intent("finish_read_nfc"))
                startActivity(intent)
                finish()
            }

            btnError.setOnClickListener {
                showAlertCancel()
            }
        }
    }

    /**
     * Carrega os dados da locação a partir do Firestore.
     *
     * @param locacaoId O ID do documento da locação a ser carregada.
     * @author Isabella
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
     * @param document O documento do Firestore contendo os dados da locação.
     * @author Isabella
     */
    private fun updateUIWithDocumentData(document: DocumentSnapshot) {
        // Obtém o ID do usuário e da unidade relacionada à locação
        val uidUsuario = document.getString("uid_usuario")
        val uidUnidade = document.getString("uid_unidade")

        // Consulta para obter os dados da pessoa com base no UID do usuário
        if (!uidUsuario.isNullOrEmpty()) {
            db.collection("Pessoas")
                .document(uidUsuario)
                .get()
                .addOnSuccessListener { pessoaDocument ->
                    if (pessoaDocument.exists()) {
                        // Se a pessoa existir, atualiza a interface com os dados da pessoa
                        val nome = pessoaDocument.getString("nome_completo")
                        val telefone = pessoaDocument.getString("celular")
                        with(binding) {
                            tvName.text = nome
                            tvPhone.text = telefone
                        }
                    } else {
                        // Caso a pessoa não seja encontrada, exibe uma mensagem de erro
                        Toast.makeText(this, "Pessoa não encontrada", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Trata o erro ao acessar os dados da pessoa
                    Toast.makeText(this, "Erro ao acessar os dados da pessoa", Toast.LENGTH_SHORT)
                        .show()
                }
        }

        // Consulta para obter os dados da unidade de locação com base no UID da unidade
        if (!uidUnidade.isNullOrEmpty()) {
            db.collection("Unidades de Locação")
                .document(uidUnidade)
                .get()
                .addOnSuccessListener { unidadeDocument ->
                    if (unidadeDocument.exists()) {
                        // Se a unidade existir, atualiza a interface com os dados da unidade de locação
                        val nomeUnidade = unidadeDocument.getString("name")
                        val endereco = unidadeDocument.getString("address")
                        with(binding) {
                            tvUnit.text = nomeUnidade
                            tvAddress.text = endereco
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Trata o erro ao acessar os dados da unidade de locação
                }
        }

        // Atualiza os elementos da UI com os dados do documento
        with(binding) {
            // Atualiza o número do armário na interface
            tvNumber.text = "Armário: ${document.getString("numero_armario")}"


            // Obtém e formata a data e hora de início da locação
            val dateLocacao = document.getString("data_locacao")
            val horaLocacao = document.getString("hora_locacao")

            if (!dateLocacao.isNullOrEmpty() && !horaLocacao.isNullOrEmpty()) {
                val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val startDate = dateTimeFormat.parse("$dateLocacao $horaLocacao")
                if (startDate != null) {
                    // Calcula a data e hora de término da locação
                    val tempoEscolhidoString =
                        document.getString("tempo_escolhido") // Obtém a string do Firestore
                    val tempoEscolhido: Int =
                        // Extrai apenas os caracteres numéricos da string e converte para inteiro
                        tempoEscolhidoString?.replace("\\D".toRegex(), "")?.toInt()
                            ?: // Define um valor padrão (0) se a string for nula
                            0

                    val calendar = Calendar.getInstance().apply {
                        time = startDate
                        add(Calendar.HOUR_OF_DAY, tempoEscolhido)
                    }
                    // Atualiza a interface com a data e hora de início, de término e o tempo de locação
                    tvStartLocation.text = "Início: ${dateTimeFormat.format(startDate)}"
                    tvTime.text = tempoEscolhidoString
                } else {
                    // Se não for possível calcular a data e hora de término, exibe uma mensagem
                }
            } else {
                // Se os dados de data e hora forem inválidos, exibe uma mensagem
                tvStartLocation.text = "Início: Dados inválidos"
            }
        }
    }

    /**
     * Adiciona uma imagem ao ViewFlipper e carrega a imagem a partir do URL do Firebase Storage.
     *
     * @param fotoUrl O URL da foto a ser carregada.
     * @author Isabella
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
                Glide.with(this@CheckDataNfcActivity)
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
        binding.cardView.visibility = View.GONE
        binding.containerButtons.visibility = View.GONE
    }

    /**
     * Esconde a barra de progresso e mostra o conteúdo principal.
     */
    private fun hideProgressBar() {
        binding.loading.visibility = View.GONE
        binding.cardView.visibility = View.VISIBLE
        binding.containerButtons.visibility = View.VISIBLE
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

    /**
     * Exibe um diálogo de confirmação de cancelamento com opções "SIM" e "NÃO".
     * Este diálogo é usado para confirmar se o usuário deseja cancelar uma operação.
     * Dependendo da escolha do usuário, a atividade pode ser finalizada e outra atividade pode ser iniciada.
     */
    private fun showAlertCancel() {
        // Inflate o layout customizado
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_cancel_operation, null)

        // Crie o AlertDialog e ajuste sua altura desejada
        val customDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Impede o fechamento do diálogo ao tocar fora dele
            .create()

        // Defina a altura desejada para o diálogo
        customDialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 600)

        // Configure os botões do diálogo
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo3)
        val btnYes = dialogView.findViewById<Button>(R.id.btnYes3)

        btnNo.setOnClickListener {
            customDialog.dismiss()
        }

        btnYes.setOnClickListener {
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("finish_read_nfc"))
            finish()
            customDialog.dismiss()
        }

        // Mostre o diálogo
        customDialog.show()
    }
}
