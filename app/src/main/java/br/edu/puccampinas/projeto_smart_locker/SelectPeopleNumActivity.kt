package br.edu.puccampinas.projeto_smart_locker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivitySelectPeopleBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

class SelectPeopleNumActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectPeopleBinding
    private lateinit var dadosCliente: DadosCliente
    private var numPessoas: Int = 0
    // Definição do BroadcastReceiver para fechar a activity a partir de outra
    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "finish_select_people_num") {
                finish()
            }
        }
    }
    // Definição do callback do botão voltar do android
    private val callback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            showAlertCancel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectPeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurando o botão voltar para sair da operação
        this.onBackPressedDispatcher.addCallback(this, callback)

        // Registra o BroadcastReceiver para finalizar a activity a partir de outra
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(closeReceiver, IntentFilter("finish_select_people_num"))

        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)

        binding.buttonConfirm.setOnClickListener {
            numPessoas = if (binding.button1person.isChecked) {
                1
            } else if (binding.button2persons.isChecked) {
                2
            } else {
                showAlertMessage()
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
     * A mensagem a ser exibida no diálogo de alerta.
     */

    private fun showAlertMessage() {
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
        textViewMessage.text = buildString {
            append("Aviso: Selecione pelo menos uma opção.")
        }

        // Mostre o diálogo
        alertDialog.show()
    }

    /**
     * Exibe um diálogo de confirmação de cancelamento com opções "SIM" e "NÃO".
     * Este diálogo é usado para confirmar se o usuário deseja cancelar uma operação.
     * Dependendo da escolha do usuário, a atividade pode ser finalizada e outra atividade pode ser iniciada.
     * O identificador do botão que acionou o diálogo de cancelamento.
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
            customDialog.dismiss()
            finish()
        }

        // Mostre o diálogo
        customDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeReceiver)
    }

    private fun startCameraActivity() {
        cameraProviderResult.launch(android.Manifest.permission.CAMERA)
    }

    private fun abrirTelaDePreview() {
        val intent = Intent(this, TakePicActivity::class.java)
        val dadosJson = Gson().toJson(dadosCliente)
        intent.putExtra("dadosCliente", dadosJson)
        intent.putExtra("numPessoas", numPessoas.toString())
        startActivity(intent)
    }
}
