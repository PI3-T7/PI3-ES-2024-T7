package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivitySelectPeopleBinding
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

class SelectPeopleNumActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectPeopleBinding
    private lateinit var dadosCliente: DadosCliente
    private var numPessoas: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectPeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)

        binding.buttonConfirm.setOnClickListener {
            if (binding.button1person.isChecked) {
                numPessoas = 1
            } else if (binding.button2persons.isChecked) {
                numPessoas = 2
            } else {
                showAlertMessage("Aviso: Selecione pelo menos uma opção.")
                return@setOnClickListener
            }
            startCameraActivity()
        }

        binding.btnCancel.setOnClickListener {
            showAlertCancel()
        }


    }

    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                abrirTelaDePreview()
            } else {
                Snackbar.make(
                    binding.root,
                    "Favor conceder permissão para abrir a câmera",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }

    /**
     * Exibe um diálogo de AVISO customizado com uma mensagem simples e um botão "OK".
     * @param message A mensagem a ser exibida no diálogo de alerta.
     */

    private fun showAlertMessage(message: String) {
        // Inflate o layout personalizado
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog_warning, null)

        // Crie o AlertDialog com o layout personalizado
        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        // Configure o botão OK para fechar o diálogo
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }

        // Atualize a mensagem no TextView
        val textViewMessage = view.findViewById<TextView>(R.id.tvMessage)
        textViewMessage.text = message

        // Mostre o diálogo
        alertDialog.show()
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
            startActivity(Intent(this,ManagerMainScreenActivity::class.java))
            finish()
            customDialog.dismiss()
        }

        // Mostre o diálogo
        customDialog.show()
    }

    private fun startCameraActivity() {
        cameraProviderResult.launch(android.Manifest.permission.CAMERA)
    }

    private fun abrirTelaDePreview() {
        val intent = Intent(this, TakePicActivity::class.java)
        val dadosJson = Gson().toJson(dadosCliente)
        intent.putExtra("dadosCliente", dadosJson)
        intent.putExtra("numPessoas", numPessoas)
        startActivity(intent)
    }
}
