package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityTakePicBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.net.Uri

class TakePicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTakePicBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    private lateinit var dadosCliente: DadosCliente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePicBinding.inflate(layoutInflater) // Infla o layout da atividade
        setContentView(binding.root) // Define o layout da atividade como o layout inflado

        // Recupera os dados do cliente da Intent
        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)

        // Configura o clique na seta de voltar para redirecionar o usuário para a tela anterior
        binding.imgArrow.setOnClickListener {
            val intent = Intent(this, SelectPeopleNumActivity::class.java)
            intent.putExtra("dadosCliente", dadosJson)
            startActivity(intent)
        }

        // Inicializa as variáveis do CameraX
        enableEdgeToEdge() // Habilita o modo de borda a borda (tela cheia)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        // Inicia a câmera
        startCamera()

        // Configura o clique do botão para tirar uma foto
        binding.buttonTakePic.setOnClickListener {
            takePhoto()
            blinkPreview()
        }
    }

    // Função para iniciar a câmera
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

    // Função para tirar uma foto
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
                        // Passa o caminho da imagem para a próxima atividade
                        val intent = Intent(this@TakePicActivity, PersonPicActivity::class.java)
                        val dadosJson = Gson().toJson(dadosCliente)
                        intent.putExtra("dadosCliente", dadosJson)
                        intent.putExtra("image_path", file.absolutePath)
                        startActivity(intent)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraPreview", "Erro ao capturar imagem: ${exception.message}", exception)
                    }
                }
            )
        }
    }

    // Função para fazer a visualização piscar brevemente
    private fun blinkPreview() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }
}