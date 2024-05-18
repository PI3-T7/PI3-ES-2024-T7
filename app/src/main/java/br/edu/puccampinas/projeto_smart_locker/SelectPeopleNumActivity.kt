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
class SelectPeopleNumActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySelectPeopleBinding
    private lateinit var dadosCliente: DadosCliente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita o modo de borda a borda (tela cheia)
        binding = ActivitySelectPeopleBinding.inflate(layoutInflater) // Infla o layout da atividade
        setContentView(binding.root) // Define o layout da atividade como o layout inflado

        // Recupera os dados do cliente da Intent
        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)

        binding.buttonConfirm.setOnClickListener {
            if (binding.button1person.isChecked) {
                // Se o botão abrir armario estiver selecionado
                cameraProviderResult.launch(android.Manifest.permission.CAMERA)

            } else if (binding.button2persons.isChecked) {
                // Se o botão encerrar locação estiver selecionado
                cameraProviderResult.launch(android.Manifest.permission.CAMERA)

            } else {
                // Se nenhum botão estiver selecionado
                Toast.makeText(this, "Por favor, selecione uma opção", Toast.LENGTH_SHORT).show()
            }
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
    // Declaração de uma variável para o resultado do contrato de permissão da câmera
    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                abrirTelaDePreview() // Se a permissão for concedida, abre a tela de visualização
            } else {
                // Se a permissão não for concedida, exibe uma mensagem Snackbar
                Snackbar.make(
                    binding.root,
                    "Favor conceder permissão para abrir a câmera",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }

    // Função para abrir a tela de visualização
    private fun abrirTelaDePreview() {
        val intent = Intent(this, TakePicActivity::class.java)
        val dadosJson = Gson().toJson(dadosCliente)
        intent.putExtra("dadosCliente", dadosJson)
        startActivity(intent)
    }
}