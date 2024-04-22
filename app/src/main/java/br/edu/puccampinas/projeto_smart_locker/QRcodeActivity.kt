package br.edu.puccampinas.projeto_smart_locker

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

class QRcodeActivity : AppCompatActivity() {

    private lateinit var imgQRcode : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        imgQRcode = findViewById(R.id.img_qr_code)

        // Obtém a string serializada da Intent
        val dados = intent.getStringExtra("dados")

        if (dados != null) {
            generateQRCode(dados)
        }
    }

    private fun generateQRCode(text: String) {
        val width = 500
        val height = 500
        val bitMatrix: BitMatrix
        try {
            val barcodeEncoder = BarcodeEncoder()
            bitMatrix = barcodeEncoder.encode(text, BarcodeFormat.QR_CODE, width, height)
            val bitmap = toBitmap(bitMatrix)
            imgQRcode.setImageBitmap(bitmap)
            imgQRcode.visibility = ImageView.VISIBLE
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

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

