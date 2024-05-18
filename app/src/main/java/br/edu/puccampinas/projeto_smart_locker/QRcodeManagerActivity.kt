package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.view.animation.AnimationUtils
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
            startActivity(Intent(this, ManagerMainScreenActivity::class.java))
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
                        val dadosReais = textoLido.removePrefix("MYAPP_")

                        val intent =
                            Intent(this@QRcodeManagerActivity, SelectPeopleNumActivity::class.java)
                        startActivity(intent)
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
                Toast.makeText(this, "Permissão da câmera concedida", Toast.LENGTH_SHORT).show()
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
     * Função que mostra uma caixa de diálogo informando que o QR code é inválido.
     */
    private fun showInvalidQRCodeDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("QR Code Inválido")
        builder.setMessage("O QR code não foi gerado pelo seu aplicativo. Deseja tentar novamente?")
        builder.setPositiveButton("Ler Novamente") { dialog, _ ->
            dialog.dismiss()
            // Reinicia a visualização do escaner
            codescanner.startPreview()
            // Reinicia a animação da linha do scanner
            binding.scannerLine.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.scanner_line_animation
                )
            )
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
            // Retorna para a tela inicial do gerente
            startActivity(Intent(this@QRcodeManagerActivity, ManagerMainScreenActivity::class.java))
        }
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.setOnDismissListener {
            // Reinicia a animação da linha do scanner quando a caixa de diálogo é fechada
            binding.scannerLine.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.scanner_line_animation
                )
            )
        }
        dialog.show()
    }
}
