package br.edu.puccampinas.projeto_smart_locker

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityPersonPicBinding
import com.google.gson.Gson
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class PersonPicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonPicBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dadosCliente: DadosCliente
    private lateinit var imagePaths: ArrayList<String>  // Alterado para armazenar a lista de caminhos de fotos
    private var numPessoas: Int = 1
    private var fotosTiradas: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonPicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)
        imagePaths = intent.getStringArrayListExtra("imagePaths") ?: ArrayList()
        numPessoas = intent.getIntExtra("numPessoas", 1)
        fotosTiradas = intent.getIntExtra("fotosTiradas", 0)

        Log.d(TAG, "numPessoas: $numPessoas")
        Log.d(TAG, "fotosTiradas: $fotosTiradas")
        Log.d("PersonPicActivity", "Caminhos das imagens: $imagePaths")

        // Exiba a última foto tirada
        if (imagePaths.isNotEmpty()) {
            val imagePath = imagePaths.last()
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val imageView: ImageView = findViewById(R.id.picture_person)
                Glide.with(this)
                    .load(imagePath)
                    .into(imageView)
            } else {
                Log.e("PersonPicActivity", "O arquivo não foi encontrado no caminho especificado.")
                Toast.makeText(this, "O arquivo não foi encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Log.e("PersonPicActivity", "Caminho da imagem não fornecido.")
            Toast.makeText(this, "Caminho da imagem não encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        updateButtonText() // Atualiza o texto do botão ao carregar a activity

        binding.buttonFinish.setOnClickListener {
            if (fotosTiradas < numPessoas) {
                // Se ainda não tirou fotos suficientes, volta para tirar mais fotos
                val intent = Intent(this, TakePicActivity::class.java)
                val dadosJson = Gson().toJson(dadosCliente)
                intent.putExtra("dadosCliente", dadosJson)
                intent.putExtra("numPessoas", numPessoas)
                intent.putExtra("fotosTiradas", fotosTiradas)
                intent.putStringArrayListExtra(
                    "imagePaths",
                    imagePaths
                )  // Passe a lista de caminhos de fotos de volta
                startActivity(intent)
            } else {
                binding.buttonFinish.isEnabled = false
                // Se tirou todas as fotos necessárias, procede com o upload e salvamento
                val calendar = Calendar.getInstance()
                val dateFormat =
                    SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.getDefault())
                val timeFormat =
                    SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault())

                val dataLocacao = dateFormat.format(calendar.time)
                val horaLocacao = timeFormat.format(calendar.time)

                obterNumeroArmarioLivre { numeroArmario ->
                    numeroArmario?.let {
                        val locacaoData = LocacaoData(
                            status = true,
                            uidUsuario = dadosCliente.id,
                            uidUnidade = dadosCliente.unidade,
                            numeroArmario = it,
                            tempoEscolhido = dadosCliente.opcao,
                            preco = dadosCliente.preco,
                            dataLocacao = dataLocacao,
                            horaLocacao = horaLocacao,
                            caucao = 0.0
                        )
                        uploadPhotosToFirebase(imagePaths, dadosCliente.id, locacaoData)
                    } ?: run {
                        Log.e("Firestore", "Nenhum armário livre encontrado.")
                        Toast.makeText(this, "Nenhum armário livre encontrado.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        binding.imgArrow.setOnClickListener {
            if (fotosTiradas > 0) {
                fotosTiradas--
                if (imagePaths.isNotEmpty()) {
                    val lastImagePath = imagePaths.removeAt(imagePaths.size - 1)
                    val lastImageFile = File(lastImagePath)
                    if (lastImageFile.exists()) {
                        lastImageFile.delete()
                    }
                }
            }
            val intent = Intent(this, TakePicActivity::class.java)
            val dadosJson = Gson().toJson(dadosCliente)
            intent.putExtra("dadosCliente", dadosJson)
            intent.putExtra("numPessoas", numPessoas)
            intent.putExtra("fotosTiradas", fotosTiradas)
            intent.putStringArrayListExtra("imagePaths", imagePaths)
            startActivity(intent)
            finish()
        }

        binding.btnCancel.setOnClickListener {
            showAlertCancel()
        }
    }

    /**
     * Atualiza o texto do botão com base na quantidade de pessoas e fotos tiradas.
     */
    private fun updateButtonText() {
        val btnText = when {
            numPessoas == 1 -> "Finalizar"
            numPessoas == 2 && fotosTiradas < numPessoas -> "Continuar"
            numPessoas == 2 && fotosTiradas == numPessoas -> "Finalizar"
            else -> binding.buttonFinish.text.toString()
        }
        binding.buttonFinish.text = btnText
    }

    /**
     * Realiza o upload das fotos para o Firebase Storage.
     *
     * @param imagePaths Lista de caminhos das fotos.
     * @param clientId ID do cliente.
     * @param locacaoData Dados da locação.
     */
    private fun uploadPhotosToFirebase(
        imagePaths: List<String>,
        clientId: String,
        locacaoData: LocacaoData
    ) {
        val photoUrls = mutableListOf<String>()
        var uploadCount = 0 // Contador para rastrear o número de uploads bem-sucedidos

        imagePaths.forEachIndexed { index, imagePath ->
            val file = File(imagePath)
            val storageRef = storage.reference
            val clientPhotoRef = storageRef.child("clients/$clientId/${file.name}")

            val uri = Uri.fromFile(file)
            val uploadTask = clientPhotoRef.putFile(uri)

            uploadTask.addOnSuccessListener { uploadTaskSnapshot ->
                clientPhotoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    photoUrls.add(downloadUrl.toString())

                    // Verifica se todas as fotos foram carregadas
                    if (++uploadCount == imagePaths.size) {
                        // Todas as fotos foram carregadas, então salva no Firestore
                        saveLocacaoToFirestore(photoUrls, locacaoData)
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("FirebaseStorage", "Erro ao fazer upload da foto $index: ${e.message}", e)

                // Em caso de falha, verifica se todas as fotos foram carregadas
                if (++uploadCount == imagePaths.size) {
                    // Todas as fotos foram carregadas, então salva no Firestore
                    saveLocacaoToFirestore(photoUrls, locacaoData)
                }
            }
        }
    }

    /**
     * Salva os dados da locação no Firestore.
     *
     * @param photoUrls Lista de URLs das fotos.
     * @param locacaoData Dados da locação.
     */
    private fun saveLocacaoToFirestore(
        photoUrls: List<String>,
        locacaoData: LocacaoData
    ) {
        val locacao = hashMapOf(
            "status" to locacaoData.status,
            "uid_usuario" to locacaoData.uidUsuario,
            "uid_unidade" to locacaoData.uidUnidade,
            "numero_armario" to locacaoData.numeroArmario,
            "tempo_escolhido" to locacaoData.tempoEscolhido,
            "preco" to locacaoData.preco,
            "fotos" to photoUrls,
            "data_locacao" to locacaoData.dataLocacao,
            "hora_locacao" to locacaoData.horaLocacao,
            "caucao" to locacaoData.caucao
        )

        firestore.collection("Locações")
            .add(locacao)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Locação salva com ID: ${documentReference.id}")
                updateArmarioStatus(locacaoData.numeroArmario.toInt())
                val intent = Intent(this, ClosetReleasedActivity::class.java)
                intent.putExtra(
                    "locacaoId",
                    documentReference.id
                ) // Envia o ID da locação para a próxima activity
                startActivity(intent)
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Erro ao salvar locação no Firestore: ${e.message}", e)
            }
    }

    /**
     * Obtém o número do armário livre.
     *
     * @param callback Função de retorno que recebe o número do armário livre.
     */
    private fun obterNumeroArmarioLivre(callback: (String?) -> Unit) {
        val nomeDocumento = dadosCliente.unidade
        firestore.collection("Unidades de Locação")
            .document(nomeDocumento)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val lockers = documentSnapshot.data?.get("lockers") as Map<String, Boolean>?

                val armariosLivres = lockers?.filterValues { it }

                val armariosOrdenados = armariosLivres?.keys?.sorted()

                val primeiroArmarioLivre = armariosOrdenados?.firstOrNull()

                callback(primeiroArmarioLivre)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erro ao obter a coleção 'Unidades de Locação': ${e.message}", e)
                callback(null)
            }
    }

    /**
     * Atualiza o status do armário no Firestore.
     *
     * @param numeroArmario Número do armário a ser atualizado.
     */
    private fun updateArmarioStatus(numeroArmario: Int) {
        val nomeDocumento = dadosCliente.unidade
        val lockersCollection = FirebaseFirestore.getInstance().collection("Unidades de Locação")
        val lockerDocument = lockersCollection.document(nomeDocumento)

        lockerDocument.update("lockers.$numeroArmario", false)
            .addOnSuccessListener {
                Log.d(TAG, "Status do armário $numeroArmario atualizado para false")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao atualizar status do armário $numeroArmario: $e")
            }
    }

    /**
     * Exibe um diálogo de confirmação de cancelamento com opções "SIM" e "NÃO".
     * Este diálogo é usado para confirmar se o usuário deseja cancelar uma operação.
     * Dependendo da escolha do usuário, a atividade pode ser finalizada e outra atividade pode ser iniciada.
     * @param button O identificador do botão que acionou o diálogo de cancelamento.
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
            // Fecha o diálogo sem fazer logout
            customDialog.dismiss()
        }

        btnYes.setOnClickListener {
            startActivity(Intent(this, ManagerMainScreenActivity::class.java))
            finish()
            customDialog.dismiss()
        }

        // Mostre o diálogo
        customDialog.show()
    }

}

/**
 * Data class para representar os dados da locação.
 */
data class LocacaoData(
    val status: Boolean,
    val uidUsuario: String,
    val uidUnidade: String,
    val numeroArmario: String,
    val tempoEscolhido: String,
    val preco: Double,
    val dataLocacao: String,
    val horaLocacao: String,
    val caucao: Double
)
