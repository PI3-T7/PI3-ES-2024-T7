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


class QRcodeManagerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityQrcodeManagerBinding.inflate(layoutInflater) }
    private lateinit var codescanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val scannerLineAnimation = AnimationUtils.loadAnimation(this, R.anim.scanner_line_animation)
        binding.scannerLine.startAnimation(scannerLineAnimation)

        binding.imgArrow.setOnClickListener {
            startActivity(Intent(this, ManagerMainScreenActivity::class.java))
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 123)
        } else {
            startScanning()
        }


    }

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
                        val dadosReais = textoLido.removePrefix("MYAPP_")
                        val intent =
                            Intent(this@QRcodeManagerActivity, SelectPeopleNumActivity::class.java)
                        startActivity(intent)
                    } else {
                        showInvalidQRCodeDialog()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão da câmera concedida", Toast.LENGTH_SHORT).show()
                startScanning()
            } else {
                Toast.makeText(this, "Permissão da câmera não concedida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::codescanner.isInitialized) {
            codescanner.startPreview()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::codescanner.isInitialized) {
            codescanner.releaseResources()
        }
    }

    private fun showInvalidQRCodeDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("QR Code Inválido")
        builder.setMessage("O QR code não foi gerado pelo seu aplicativo. Deseja tentar novamente?")
        builder.setPositiveButton("Ler Novamente") { dialog, _ ->
            dialog.dismiss()
            codescanner.startPreview()
            binding.scannerLine.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.scanner_line_animation
                )
            )
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
            startActivity(Intent(this@QRcodeManagerActivity, ManagerMainScreenActivity::class.java))
        }
        builder.setCancelable(false) // Impede que a caixa de diálogo seja fechada ao tocar fora dela
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
