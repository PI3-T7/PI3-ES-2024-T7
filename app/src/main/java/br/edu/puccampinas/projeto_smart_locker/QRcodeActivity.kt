package br.edu.puccampinas.projeto_smart_locker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog


class QRcodeActivity : AppCompatActivity() {

    private lateinit var imgQRcode: ImageView
    private lateinit var cancelLocation: ImageView
    private lateinit var buttonVoltar2: ImageView
    private var qrCodeBitmap: Bitmap? = null // Declaração da variável qrCodeBitmap
    // chaves para SharedPreferences
    private val sharedPref = "Locacao"
    private val qrCodeBitMapKey = "locacaoPendente"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        imgQRcode = findViewById(R.id.img_qr_code)
        cancelLocation = findViewById(R.id.cancelLocation)
        buttonVoltar2 = findViewById(R.id.buttonVoltar2)

        // Obtém a string serializada da Intent
        val dados = intent.getStringExtra("dados")
        Log.d("QRcodeActivity", "Dados recebidos: $dados")

        // Verifica se os dados são nulos e chama a função
        if (dados != null) {
            generateQRCode(dados)
        }

        buttonVoltar2.setOnClickListener {
            finish()
        }

        cancelLocation.setOnClickListener {
            showAlertCancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Define o nome das SharedPreferences e as chaves corretas
        val sharedPref = "Locacao"
        val qrCodeBitMapKey = "qrCodeBitmap" // Chave para o bitmap do QR code
        val locacaoPendenteKey = "locacaoPendente" // Chave para o status de locação pendente

        // Salva o status de locação pendente nas SharedPreferences
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(locacaoPendenteKey, qrCodeBitmap != null)
        editor.apply()

        // Verifica se há um QR code bitmap para salvar
        qrCodeBitmap?.let { bitmap ->
            // Salva o QR code bitmap nas SharedPreferences
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val encodedBitmap = Base64.encodeToString(byteArray, Base64.DEFAULT)
            editor.putString(qrCodeBitMapKey, encodedBitmap)
            editor.apply()
        }
    }

    override fun onStop() {
        super.onStop()

        // Define o nome das SharedPreferences e as chaves corretas
        val sharedPref = "Locacao"
        val qrCodeBitMapKey = "qrCodeBitmap" // Chave para o bitmap do QR code
        val locacaoPendenteKey = "locacaoPendente" // Chave para o status de locação pendente

        // Salva o status de locação pendente nas SharedPreferences
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(locacaoPendenteKey, qrCodeBitmap != null)
        editor.apply()

        // Verifica se há um QR code bitmap para salvar
        qrCodeBitmap?.let { bitmap ->
            // Salva o QR code bitmap nas SharedPreferences
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val encodedBitmap = Base64.encodeToString(byteArray, Base64.DEFAULT)
            editor.putString(qrCodeBitMapKey, encodedBitmap)
            editor.apply()
        }
    }

    override fun onResume() {
        super.onResume()

        // Defina o nome das SharedPreferences e as chaves corretas
        val sharedPref = "Locacao"
        val qrCodeBitMapKey = "qrCodeBitmap" // Chave para o bitmap do QR code
        val locacaoPendenteKey = "locacaoPendente" // Chave para o status de locação pendente

        // Recupera o QR code bitmap das SharedPreferences
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val encodedBitmap = prefs.getString(qrCodeBitMapKey, null)

        // Recupera o status de locação pendente das SharedPreferences
        val locacaoPendente = prefs.getBoolean(locacaoPendenteKey, false)

        // Verifica se o QR code bitmap está disponível
        if (encodedBitmap != null) {
            // Decodifica o bitmap do Base64
            val decodedByteArray = Base64.decode(encodedBitmap, Base64.DEFAULT)
            val decodedBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)

            // Define o bitmap na ImageView
            imgQRcode.setImageBitmap(decodedBitmap)
            imgQRcode.visibility = ImageView.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()

        // Defina o nome das SharedPreferences e as chaves corretas
        val sharedPref = "Locacao"
        val qrCodeBitMapKey = "qrCodeBitmap" // Chave para o bitmap do QR code
        val locacaoPendenteKey = "locacaoPendente" // Chave para o status de locação pendente

        // Recupera o QR code bitmap das SharedPreferences
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val encodedBitmap = prefs.getString(qrCodeBitMapKey, null)

        // Recupera o status de locação pendente das SharedPreferences
        val locacaoPendente = prefs.getBoolean(locacaoPendenteKey, false)

        // Verifica se o QR code bitmap está disponível
        if (encodedBitmap != null) {
            // Decodifica o bitmap do Base64
            val decodedByteArray = Base64.decode(encodedBitmap, Base64.DEFAULT)
            val decodedBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)

            // Define o bitmap na ImageView
            imgQRcode.setImageBitmap(decodedBitmap)
            imgQRcode.visibility = ImageView.VISIBLE
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
            cancelLocacaoPendente()
            startActivity(Intent(this,ClientMainScreenActivity::class.java))
            finish()
            customDialog.dismiss()
        }

        // Mostre o diálogo
        customDialog.show()
    }

    private fun cancelLocacaoPendente() {
        // Cancelando locação pendente
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(
            qrCodeBitMapKey,
            false
        ) // Define como false para cancelar a locação pendente
        editor.apply()
    }
}
