
package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityQrcodeManagerBinding
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class QRcodeManagerActivity : AppCompatActivity() {
    private val binding by lazy { ActivityQrcodeManagerBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_manager)

        binding.imgArrow.setOnClickListener {
            startActivity(Intent(this,ManagerMainScreenActivity::class.java))
        }

        binding.btnExcluir.setOnClickListener {
            startActivity(Intent(this,SelectPeopleNumActivity::class.java))
        }
        // Inicia o escaneamento do QR code
        IntentIntegrator(this).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // Escaneamento cancelado
                Toast.makeText(this, "Escaneamento cancelado", Toast.LENGTH_LONG).show()
            } else {
                // Obtem o conteúdo do QR code (string JSON)
                val dadosJson = result.contents
                // Desserializa a string JSON para um objeto DadosCliente
                val dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)

                // Envia os dados do cliente para a próxima atividade
                val intent = Intent(this, SelectPeopleNumActivity::class.java)
                intent.putExtra("dadosCliente", dadosJson)
                startActivity(intent)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}