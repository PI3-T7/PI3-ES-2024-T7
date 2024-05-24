package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityTakePicBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import java.io.File
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
    private var numPessoas: Int = 1
    private var fotosTiradas: Int = 0
    private lateinit var imagePaths: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recebe os dados do cliente e informações sobre a quantidade de pessoas e fotos tiradas
        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)
        numPessoas = intent.getIntExtra("numPessoas", 1)
        fotosTiradas = intent.getIntExtra("fotosTiradas", 0)
        imagePaths = intent.getStringArrayListExtra("imagePaths") ?: ArrayList()

        // Inicializa as variáveis relacionadas à câmera
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

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

        // Atualiza o texto informativo sobre a captura de fotos
        updateTextTakeThePhoto()
    }

    /**
     * Inicia a câmera e configura os casos de uso (Preview e ImageCapture).
     */
    private fun startCamera() {
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
                        val savedUri = outputFileResults.savedUri ?: Uri.fromFile(file)
                        val msg = "Foto salva com sucesso: $savedUri"
                        Log.d("CameraPreview", msg)

                        if (numPessoas == 1 && imagePaths.isNotEmpty()) {
                            val lastImagePath = imagePaths.removeAt(imagePaths.size - 1)
                            val lastImageFile = File(lastImagePath)
                            if (lastImageFile.exists()) {
                                lastImageFile.delete()
                            }
                        }

                        // Incrementa o contador de fotos tiradas e adiciona o caminho da foto à lista
                        fotosTiradas++
                        imagePaths.add(file.absolutePath)

                        // Navega para a próxima atividade, passando os dados necessários
                        val intent = Intent(this@TakePicActivity, PersonPicActivity::class.java)
                        val dadosJson = Gson().toJson(dadosCliente)
                        intent.putExtra("dadosCliente", dadosJson)
                        intent.putExtra("imagePaths", imagePaths)
                        intent.putExtra("numPessoas", numPessoas)
                        intent.putExtra("fotosTiradas", fotosTiradas)
                        startActivity(intent)
                        finish()
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
    private fun updateTextTakeThePhoto() {
        val msg = when {
            numPessoas == 1 -> "Tire uma foto da pessoa"
            numPessoas == 2 && fotosTiradas == 0 -> "Tire uma foto da primeira pessoa"
            numPessoas == 2 && fotosTiradas == 1 -> "Tire uma foto da segunda pessoa"
            else -> ""
        }
        binding.textTakeThePic.text = msg
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
            finish()
        }

        // Mostre o diálogo
        customDialog.show()
    }
}
