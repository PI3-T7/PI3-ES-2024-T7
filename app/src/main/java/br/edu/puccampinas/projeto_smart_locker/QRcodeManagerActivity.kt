package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityQrcodeManagerBinding
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

/**
 * Activity responsável pela leitura do QR code.
 */
class QRcodeManagerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityQrcodeManagerBinding.inflate(layoutInflater) }
    private lateinit var codescanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Inicia a animação da linha do scanner
        val scannerLineAnimation = AnimationUtils.loadAnimation(this, R.anim.scanner_line_animation)
        binding.scannerLine.startAnimation(scannerLineAnimation)

        binding.imgArrow.setOnClickListener {
            finish()
        }

        // Verifica se a permissão da câmera foi concedida
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            // Solicita permissão da câmera se não foi concedida
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 123)
        } else {
            // Inicia a função de escaneamento se a permissão foi concedida
            startScanning()
        }
    }

    /**
     * Inicia a função de escaneamento do QR code.
     */
    private fun startScanning() {
        val scannerView: CodeScannerView = findViewById(R.id.scanner_view)
        codescanner = CodeScanner(this, scannerView).apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false
            decodeCallback = DecodeCallback { result ->
                runOnUiThread {
                    val textoLido = result.text
                    if (textoLido.startsWith("SMARTLOCKER_")) {
                        // O QR code foi gerado pelo seu aplicativo
                        val dadosReais = textoLido.removePrefix("SMARTLOCKER_")

                        // Verifica a disponibilidade de armários na unidade de locação
                        verificarDisponibilidadeArmarios(dadosReais)

                    } else {
                        // Mostra a caixa de diálogo para QR code inválido
                        showInvalidQRCodeDialog()
                        // Para a animação da linha do scanner
                        binding.scannerLine.clearAnimation()
                    }
                }
            }
            errorCallback = ErrorCallback {
                runOnUiThread {
                    Toast.makeText(
                        this@QRcodeManagerActivity,
                        "Error: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        codescanner.startPreview()
    }

    /**
     * Função que lida com a resposta da solicitação de permissão da câmera.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão da câmera concedida, inicia a função de escaneamento
                startScanning()
            } else {
                // Permissão da câmera não concedida, mostra uma mensagem
                Toast.makeText(this, "Permissão da câmera não concedida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Reinicia a visualização do escâner ao retomar a atividade.
     */
    override fun onResume() {
        super.onResume()
        if (::codescanner.isInitialized) {
            codescanner.startPreview()
        }
    }

    /**
     * Pausa o escaneamento ao pausar a atividade.
     */
    override fun onPause() {
        super.onPause()
        if (::codescanner.isInitialized) {
            codescanner.releaseResources()
        }
    }

    /**
     * Exibe um diálogo de qrcode inválido com opções "SIM" e "NÃO".
     * Este diálogo é usado para perguntar se o usuário deseja tentar novamente.
     * @authors: Lais e Isabella
     */
    private fun showInvalidQRCodeDialog() {
        // Inflate o layout customizado
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_invalid_qrcode, null)

        // Crie o AlertDialog e ajuste sua altura desejada
        val customDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Impede o fechamento do diálogo ao tocar fora dele
            .create()

        // Configure os botões do diálogo
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo3)
        val btnYes = dialogView.findViewById<Button>(R.id.btnYes3)

        btnNo.setOnClickListener {
            // Fecha o diálogo e retorna para a página inicial do gerente
            customDialog.dismiss()
            finish()
        }

        btnYes.setOnClickListener {
            customDialog.dismiss()
            // Reinicia a visualização do scanner
            codescanner.startPreview()
            // Reinicia a animação da linha do scanner
            binding.scannerLine.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.scanner_line_animation)
            )
        }

        customDialog.setOnDismissListener {
            // Reinicia a animação da linha do scanner quando a caixa de diálogo é fechada
            binding.scannerLine.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.scanner_line_animation)
            )
        }

        // Mostre o diálogo e ajuste a altura desejada
        customDialog.show()
        customDialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            700
        )
    }

    /**
     * Função que lida com a resposta do IntentIntegrator para escaneamento de QR code.
     */
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result.contents == null) {
            // Escaneamento cancelado
            Toast.makeText(this, "Escaneamento cancelado", Toast.LENGTH_LONG).show()
        } else {
            // Obtem o conteúdo do QR code (string JSON)
            val dadosJson = result.contents

            // Envia os dados do cliente para a próxima atividade
            val intent = Intent(this, SelectPeopleNumActivity::class.java)
            intent.putExtra("dadosCliente", dadosJson)
            startActivity(intent)
            finish()
        }
    }
    /**
    * Esta função verifica a disponibilidade de armários na unidade de locação fornecida pelo QR code.
    * Ela extrai os dados da unidade de locação do JSON recebido, realiza uma consulta ao Firestore
    * para obter os dados da unidade de locação e verifica a disponibilidade de armários.
    * Se houver armários disponíveis, avança para a tela de seleção do número de pessoas.
    * Caso contrário, exibe um diálogo de aviso informando que não há mais armários disponíveis na
    * unidade de locação. Em caso de falha ao obter os dados da unidade de locação, exibe um Toast com uma mensagem de erro.
    */
    private fun verificarDisponibilidadeArmarios(dadosReais: String) {
        // Extrai os dados da unidade de locação do QR code
        val dadosCliente = Gson().fromJson(dadosReais, DadosCliente::class.java)

        // Verifica a disponibilidade de armários na unidade de locação
        val nomeDocumento = dadosCliente.unidade
        FirebaseFirestore.getInstance().collection("Unidades de Locação")
            .document(nomeDocumento)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val lockers = documentSnapshot.data?.get("lockers") as Map<*, *>?

                // Verifica se há armários disponíveis
                if (lockers != null && lockers.containsValue(true)) {
                    // Há armários disponíveis, avança para a seleção do número de pessoas
                    val intent = Intent(this@QRcodeManagerActivity, SelectPeopleNumActivity::class.java)
                    intent.putExtra("dadosCliente", dadosReais)
                    startActivity(intent)
                    finish()
                } else {
                    // Não há armários disponíveis na unidade de locação
                    showAlertMessage()
                }
            }
            .addOnFailureListener { e ->
                // Trata o erro ao obter os dados da unidade de locação
                Toast.makeText(this@QRcodeManagerActivity, "Erro ao verificar armários diponíveis: ${e.message}", Toast.LENGTH_SHORT).show()
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

        // Configure o botão OK para fechar o diálogo e voltar para a tela anterior
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        btnOk.setOnClickListener {
            alertDialog.dismiss()
            // Adicione um Intent para voltar para a tela anterior
            finish()
        }

        // Atualize a mensagem no TextView
        val textViewMessage = view.findViewById<TextView>(R.id.tvMessage)
        textViewMessage.text = buildString {
            append("Aviso: Não há mais armários disponíveis nessa unidade!")
        }

        // Mostre o diálogo
        alertDialog.show()
    }

}