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


class QRcodeActivity : AppCompatActivity() {

    private lateinit var imgQRcode: ImageView
    private lateinit var buttonHome2: ImageView
    private lateinit var buttonVoltar2: ImageView
    private var qrCodeBitmap: Bitmap? = null // Declaração da variável qrCodeBitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        imgQRcode = findViewById(R.id.img_qr_code)
        buttonHome2 = findViewById(R.id.buttonHome2)
        buttonVoltar2 = findViewById(R.id.buttonVoltar2)

        // Obtém a string serializada da Intent
        val dados = intent.getStringExtra("dados")

        // Verifica se os dados são nulos e chama a função
        if (dados != null) {
            generateQRCode(dados)
        }

        buttonVoltar2.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonHome2.setOnClickListener {
            val intent = Intent(this, ClientMainScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Defina o nome das SharedPreferences e a chave do QR code bitmap
        val sharedPref = "Locacao"
        val qrCodeBitMapKey = "qrCodeBitmap" // Corrigido para qrCodeBitmap

        // Verifica se há um QR code bitmap para salvar
        qrCodeBitmap?.let { bitmap ->
            // Salva o QR code bitmap nas SharedPreferences
            val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val encodedBitmap = Base64.encodeToString(byteArray, Base64.DEFAULT)
            editor.putString(qrCodeBitMapKey, encodedBitmap)
            editor.putBoolean("locacaoPendente", true) // Defina como true se houver uma locação pendente
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


    // Função que gera um QRcode
    private fun generateQRCode(text: String) {
        val width = 500
        val height = 500
        val bitMatrix: BitMatrix
        try {
            val barcodeEncoder = BarcodeEncoder()
            bitMatrix = barcodeEncoder.encode(text, BarcodeFormat.QR_CODE, width, height)
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
}
