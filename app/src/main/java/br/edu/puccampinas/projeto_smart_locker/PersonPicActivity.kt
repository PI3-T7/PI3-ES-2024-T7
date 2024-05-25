package br.edu.puccampinas.projeto_smart_locker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityPersonPicBinding
import com.bumptech.glide.Glide
import java.io.File

class PersonPicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonPicBinding
    private lateinit var status: String
    private lateinit var imagePaths: ArrayList<String>
    private val broadcastFunction by lazy { LocalBroadcastManager.getInstance(this) }
    // Definição do BroadcastReceiver para fechar a activity a partir de outra
    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "finish_person_pic") { finish() }
        }
    }
    // Definição do callback do botão voltar do android
    private val callback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            LocalBroadcastManager.getInstance(this@PersonPicActivity).sendBroadcast(Intent("deleteFoto"))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonPicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurando o botão voltar para sair da conta do app
        this.onBackPressedDispatcher.addCallback(this, callback)

        // Registra o BroadcastReceiver para finalizar a activity a partir de outra
        broadcastFunction.registerReceiver(closeReceiver, IntentFilter("finish_person_pic"))

        status = intent.getStringExtra("status").toString()
        imagePaths = intent.getStringArrayListExtra("imagePaths") ?: ArrayList()

        if (status == "1/2") {
            binding.buttonFinish.text = buildString {
                append("Continuar")
            }
        }
        // Exiba a última foto tirada
        if (imagePaths.isNotEmpty()) {
            val imagePath = imagePaths.last()
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val imageView: ImageView = findViewById(R.id.picture_person)
                Glide.with(this)
                    .load(imagePath)
                    .into(imageView)
            } else {
                Log.e("PersonPicActivity", "O arquivo não foi encontrado no caminho especificado.")
                Toast.makeText(this, "O arquivo não foi encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Log.e("PersonPicActivity", "Caminho da imagem não fornecido.")
            Toast.makeText(this, "Caminho da imagem não encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.buttonFinish.setOnClickListener {
            savePic()
        }

        binding.imgArrow.setOnClickListener {
            broadcastFunction.sendBroadcast(Intent("deleteFoto"))
            finish()
        }

        binding.btnCancel.setOnClickListener {
            showAlertCancel()
        }
    }

    private fun savePic() {
        binding.progressBar.visibility = View.VISIBLE
        when (status) {
            "1/1" -> {
                binding.buttonFinish.isEnabled = false
                broadcastFunction.sendBroadcast(Intent("oneOfOne"))
            }
            "1/2" -> {
                broadcastFunction.sendBroadcast(Intent("oneOfTwo"))
                finish()
            }
            else -> {
                binding.buttonFinish.isEnabled = false
                broadcastFunction.sendBroadcast(Intent("twoOfTwo"))
            }
        }
    }


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
            broadcastFunction.sendBroadcast(Intent("finish_take_pic"))
            broadcastFunction.sendBroadcast(Intent("finish_select_people_num"))
            finish()
        }

        // Mostre o diálogo
        customDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastFunction.unregisterReceiver(closeReceiver)
    }

}

/**
 * Data class para representar os dados da locação.
 */
data class LocacaoData(
    val status: Boolean,
    val uidUsuario: String,
    val uidUnidade: String,
    val numeroArmario: String,
    val tempoEscolhido: String,
    val preco: Double,
    val dataLocacao: String,
    val horaLocacao: String,
    val caucao: Double
)
