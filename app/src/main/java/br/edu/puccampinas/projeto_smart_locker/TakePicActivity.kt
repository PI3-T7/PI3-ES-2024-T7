package br.edu.puccampinas.projeto_smart_locker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityTakePicBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Activity responsável por capturar uma foto da pessoa ou pessoas que irão acessar o armário.
 */
class TakePicActivity : AppCompatActivity() {

    // Declaração das variáveis
    private lateinit var binding: ActivityTakePicBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    private lateinit var dadosCliente: DadosCliente
    private lateinit var imagePaths: ArrayList<String>
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance() }
    private val brodcastFunction by lazy { LocalBroadcastManager.getInstance(this) }
    // Definição do status de cadastro de fotos (1/1, 1/2 ou 2/2)
    private lateinit var status: String
    // Definição do BroadcastReceiver para fechar a activity a partir de outra
    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "finish_take_pic") {
                finish()
            }
        }
    }
    // Definição do BroadcastReceiver para executar a função oneOfOne a partir de outra ativity
    private val oneOfOneReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "oneOfOne") {
                oneOfOne()
            }
        }
    }
    // Definição do BroadcastReceiver para executar a função oneOfTwo a partir de outra ativity
    private val oneOfTwoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "oneOfTwo") {
                oneOfTwo()
            }
        }
    }
    // Definição do BroadcastReceiver para executar a função twoOfTwo a partir de outra ativity
    private val twoOfTwoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "twoOfTwo") {
                twoOfTwo()
            }
        }
    }
    // Definição do BroadcastReceiver para apagar uma foto do imagePath
    private val deleteFotoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "deleteFoto") {
                deleteFoto()
            }
        }
    }
    // Definição do callback do botão voltar do android
    private val callback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            goBack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        status = "1/${intent.getStringExtra("numPessoas")}"

        // Configurando o botão voltar para sair da conta do app
        this.onBackPressedDispatcher.addCallback(this, callback)

        // Registra o BroadcastReceiver para finalizar a activity a partir de outra
        brodcastFunction.registerReceiver(closeReceiver, IntentFilter("finish_take_pic"))

        // Registra o BroadcastReceiver para realizar a função oneOfOne
        brodcastFunction.registerReceiver(oneOfOneReceiver, IntentFilter("oneOfOne"))

        // Registra o BroadcastReceiver para finalizar a activity a partir de outra
        brodcastFunction.registerReceiver(oneOfTwoReceiver, IntentFilter("oneOfTwo"))

        // Registra o BroadcastReceiver para finalizar a activity a partir de outra
        brodcastFunction.registerReceiver(twoOfTwoReceiver, IntentFilter("twoOfTwo"))

        // Registra o BroadcastReceiver para finalizar a activity a partir de outra
        brodcastFunction.registerReceiver(deleteFotoReceiver, IntentFilter("deleteFoto"))

        // Inicializa as variáveis relacionadas à câmera
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        // Recebe os dados do cliente e informações sobre a quantidade de pessoas e fotos tiradas
        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)
        imagePaths = intent.getStringArrayListExtra("imagePaths") ?: ArrayList()


        // Inicia a câmera
        startCamera()

        // Configura o clique no botão de capturar foto
        binding.buttonTakePic.setOnClickListener {
            takePhoto()
            blinkPreview()
        }

        binding.btnCancel.setOnClickListener {
            showAlertCancel()
        }

        binding.imgArrow.setOnClickListener{goBack()}
    }


    /**
     * Inicia a câmera e configura os casos de uso (Preview e ImageCapture).
     */
    private fun startCamera() {
        Log.i("teste", "passou pelo startCamera")
        cameraProviderFuture.addListener({
            imageCapture = ImageCapture.Builder().build()
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e("CameraPreview", "Falha ao abrir a câmera.", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Captura uma foto utilizando o ImageCapture e salva o arquivo da imagem.
     */
    private fun takePhoto() {
        imageCapture?.let {
            val fileName = "FOTO_JPEG_${System.currentTimeMillis()}.jpg"
            val file = File(externalMediaDirs[0], fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                        imagePaths.add(file.absolutePath)

                        // Navega para a próxima atividade, passando os dados necessários
                        val intent = Intent(this@TakePicActivity, PersonPicActivity::class.java)
                        intent.putExtra("imagePaths", imagePaths)
                        intent.putExtra("status", status)
                        startActivity(intent)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(
                            "CameraPreview",
                            "Erro ao capturar imagem: ${exception.message}",
                            exception
                        )
                    }
                }
            )
        }
    }

    /**
     * Realiza um breve efeito de "blink" na visualização da câmera.
     */
    private fun blinkPreview() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }

    /**
     * Atualiza o texto informativo sobre a captura de fotos com base na quantidade de pessoas e fotos tiradas.
     */
    override fun onStart() {
        super.onStart()
        val msg = when (status){
            "1/1" -> "Tire uma foto da pessoa"
            "1/2" -> "Tire uma foto da primeira pessoa"
            "2/2" -> "Tire uma foto da segunda pessoa"
            else -> ""
        }
        binding.textTakeThePic.text = msg
    }

    private fun oneOfOne(){
        // Se tirou todas as fotos necessárias, procede com o upload e salvamento
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

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

    private fun oneOfTwo(){
        status = "2/2"
    }

    private fun twoOfTwo(){
        // Se tirou todas as fotos necessárias, procede com o upload e salvamento
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

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

            uploadTask.addOnSuccessListener {
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

        database.collection("Locações")
            .add(locacao)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Locação salva com ID: ${documentReference.id}")
                updateArmarioStatus(locacaoData.numeroArmario.toInt())
                val intent = Intent(this, WriteNfcActivity::class.java)
                intent.putExtra("location_data", documentReference.id)
                when (status){
                    "1/1" -> intent.putExtra("qtdeTags", 1)
                    else -> intent.putExtra("qtdeTags", 2)
                }
                brodcastFunction.sendBroadcast(Intent("finish_person_pic"))
                brodcastFunction.sendBroadcast(Intent("finish_select_people_num"))
                startActivity(intent)
                finish()
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
        database.collection("Unidades de Locação")
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
                Log.d("Firestore Update", "Status do armário $numeroArmario atualizado para false")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore Error", "Erro ao atualizar status do armário $numeroArmario: $e")
            }
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
            customDialog.dismiss()
            brodcastFunction.sendBroadcast(Intent("finish_select_people_num"))
            finish()
        }

        // Mostre o diálogo
        customDialog.show()
    }

    private fun goBack() {
        if (status == "2/2"){
            status = "1/2"
            val intent = Intent(this@TakePicActivity, PersonPicActivity::class.java)
            intent.putExtra("imagePaths", imagePaths)
            intent.putExtra("status", status)
            startActivity(intent)
        }else finish()
    }

    private fun deleteFoto(){
        if (imagePaths.isNotEmpty()) {
            val lastImagePath = imagePaths.removeAt(imagePaths.size - 1)
            val lastImageFile = File(lastImagePath)
            if (lastImageFile.exists()) {
                lastImageFile.delete()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        brodcastFunction.unregisterReceiver(closeReceiver)
        brodcastFunction.unregisterReceiver(oneOfOneReceiver)
        brodcastFunction.unregisterReceiver(oneOfTwoReceiver)
        brodcastFunction.unregisterReceiver(twoOfTwoReceiver)
        brodcastFunction.unregisterReceiver(deleteFotoReceiver)
    }
}
