package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivitySelectPeopleBinding
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson


/**
 * Activity responsável por permitir ao usuário selecionar o número de pessoas para alugar armários.
 */
class SelectPeopleNumActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectPeopleBinding
    private lateinit var dadosCliente: DadosCliente
    private var numPessoas: Int = 0
    private var pendenciaId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectPeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera os dados do cliente passados da activity anterior
        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)
        pendenciaId = intent.getStringExtra("pendenciaId")

        binding.buttonConfirm.setOnClickListener {
            if (binding.button1person.isChecked) {
                numPessoas = 1
            } else if (binding.button2persons.isChecked) {
                numPessoas = 2
            } else {
                Toast.makeText(this, "Por favor, selecione uma opção", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startCameraActivity()
        }

        binding.buttonHome2.setOnClickListener {
            startActivity(Intent(this, ManagerMainScreenActivity::class.java))
            finish()
        }

        binding.buttonVoltar2.setOnClickListener {
            startActivity(Intent(this, QRcodeManagerActivity::class.java))
            finish()
        }
    }

    // Resultado da solicitação de permissão da câmera
    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                abrirTelaDePreview() // Abre a tela de visualização da câmera
            } else {
                Snackbar.make(
                    binding.root,
                    "Favor conceder permissão para abrir a câmera",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }

    // Inicia a atividade da câmera
    private fun startCameraActivity() {
        cameraProviderResult.launch(android.Manifest.permission.CAMERA)
    }

    // Abre a tela de pré-visualização da câmera
    private fun abrirTelaDePreview() {
        val intent = Intent(this, TakePicActivity::class.java)
        val dadosJson = Gson().toJson(dadosCliente)
        intent.putExtra("dadosCliente", dadosJson)
        intent.putExtra("numPessoas", numPessoas)
        intent.putExtra("pendenciaId", pendenciaId)
        startActivity(intent)
    }
}
