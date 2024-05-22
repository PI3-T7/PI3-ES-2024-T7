package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
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

class TakePicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTakePicBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    private lateinit var dadosCliente: DadosCliente
    private var numPessoas: Int = 1
    private var fotosTiradas: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)
        numPessoas = intent.getIntExtra("numPessoas", 1)
        fotosTiradas = intent.getIntExtra("fotosTiradas", 0) // Pegue o valor de fotosTiradas do Intent

        binding.imgArrow.setOnClickListener {
            val intent = Intent(this, SelectPeopleNumActivity::class.java)
            intent.putExtra("dadosCliente", dadosJson)
            startActivity(intent)
        }

        enableEdgeToEdge()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        binding.buttonTakePic.setOnClickListener {
            takePhoto()
            blinkPreview()
        }

        updateTextTakeThePhoto()  // Chama a função para atualizar o texto inicialmente
    }

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

                        fotosTiradas++  // Incrementa o número de fotos tiradas

                        val intent = Intent(this@TakePicActivity, PersonPicActivity::class.java)
                        val dadosJson = Gson().toJson(dadosCliente)
                        intent.putExtra("dadosCliente", dadosJson)
                        intent.putExtra("image_path", file.absolutePath)
                        intent.putExtra("numPessoas", numPessoas)
                        intent.putExtra("fotosTiradas", fotosTiradas) // Adiciona fotosTiradas ao Intent
                        startActivity(intent)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraPreview", "Erro ao capturar imagem: ${exception.message}", exception)
                    }
                }
            )
        }
    }

    private fun blinkPreview() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }

    private fun updateTextTakeThePhoto() {
        val msg = when {
            numPessoas == 1 -> "Tire uma foto da pessoa"
            numPessoas == 2 && fotosTiradas == 0 -> "Tire uma foto da primeira pessoa"
            numPessoas == 2 && fotosTiradas == 1 -> "Tire uma foto da segunda pessoa"
            else -> ""
        }
        binding.textTakeThePic.text = msg
    }
}
