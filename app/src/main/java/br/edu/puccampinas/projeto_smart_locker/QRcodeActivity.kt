package br.edu.puccampinas.projeto_smart_locker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import java.io.ByteArrayOutputStream


class QRcodeActivity : AppCompatActivity() {

    private lateinit var imgQRcode: ImageView
    private lateinit var cancelLocation: ImageView
    private lateinit var buttonVoltar2: ImageView
    private var qrCodeBitmap: Bitmap? = null // Declaração da variável qrCodeBitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        imgQRcode = findViewById(R.id.img_qr_code)
        cancelLocation = findViewById(R.id.cancelLocation)
        buttonVoltar2 = findViewById(R.id.buttonVoltar2)

        val sharedPreferences = getSharedPreferences("SmartLockerPrefs", Context.MODE_PRIVATE)
        val pendingRental = sharedPreferences.getBoolean("pending_rental", false)

        if (pendingRental) {
            // Carregar QR code de SharedPreferences
            val qrCodeString = sharedPreferences.getString("qr_code_bitmap", null)
            if (qrCodeString != null) {
                val bitmap = decodeBase64(qrCodeString)
                imgQRcode.setImageBitmap(bitmap)
                imgQRcode.visibility = ImageView.VISIBLE
                qrCodeBitmap = bitmap
            }
        } else {
            // Obtém a string serializada da Intent
            val dados = intent.getStringExtra("dados")
            Log.d("QRcodeActivity", "Dados recebidos: $dados")

            // Verifica se os dados são nulos e chama a função
            if (dados != null) {
                generateQRCode(dados)
                savePendingRental()
                saveQRCodeToSharedPreferences(qrCodeBitmap)
            }
        }

        buttonVoltar2.setOnClickListener {
            clearPendingRental()
            startActivity(Intent(this@QRcodeActivity, LocationActivity::class.java))
            finish()
        }

        cancelLocation.setOnClickListener {
            clearPendingRental()
            showAlertCancel()
        }
    }

    // Função que gera um QRcode
    private fun generateQRCode(text: String) {
        val prefixo = "SMARTLOCKER_"
        val dadosComPrefixo = prefixo + text // Adiciona o prefixo aos dados
        val width = 500
        val height = 500
        val bitMatrix: BitMatrix
        try {
            val barcodeEncoder = BarcodeEncoder()
            bitMatrix = barcodeEncoder.encode(dadosComPrefixo, BarcodeFormat.QR_CODE, width, height)
            val bitmap = toBitmap(bitMatrix)
            qrCodeBitmap = bitmap // Atribui o bitmap gerado à variável qrCodeBitmap
            imgQRcode.setImageBitmap(bitmap)
            imgQRcode.visibility = ImageView.VISIBLE
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    // Função que faz a conversão uma matriz de bits (BitMatrix) em um objeto Bitmap
    private fun toBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFF9F3.toInt())
            }
        }
        return bmp
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
            // Fecha o diálogo sem cancelar a operação
            customDialog.dismiss()
        }

        btnYes.setOnClickListener {
            clearPendingRental()
            startActivity(Intent(this@QRcodeActivity, ClientMainScreenActivity::class.java))
            finish()
            customDialog.dismiss()
        }

        // Mostre o diálogo
        customDialog.show()
    }

    // Função para salvar o estado de locação pendente
    private fun savePendingRental() {
        val sharedPreferences = getSharedPreferences("SmartLockerPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("pending_rental", true)
        editor.apply()
    }

    // Função para limpar o estado de locação pendente
    private fun clearPendingRental() {
        val sharedPreferences = getSharedPreferences("SmartLockerPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("pending_rental", false)
        editor.apply()
    }

    // Função para salvar o QR code em SharedPreferences
    private fun saveQRCodeToSharedPreferences(bitmap: Bitmap?) {
        if (bitmap != null) {
            val sharedPreferences = getSharedPreferences("SmartLockerPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val qrCodeString = encodeToBase64(bitmap)
            editor.putString("qr_code_bitmap", qrCodeString)
            editor.apply()
        }
    }

    // Função para codificar Bitmap em Base64
    private fun encodeToBase64(image: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    // Função para decodificar string Base64 em Bitmap
    private fun decodeBase64(input: String): Bitmap {
        val decodedByte = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }
}
