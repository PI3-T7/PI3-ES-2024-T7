package br.edu.puccampinas.projeto_smart_locker

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth

class ClientMainScreenActivity : AppCompatActivity() {

    private val sharedPref = "Locacao"
    private val qrCodeBitMapKey = "locacaoPendente"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_main_screen)

        // Verifica se há uma locação pendente
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val locacaoPendente = prefs.getBoolean(qrCodeBitMapKey, false)

        if (locacaoPendente) {
            // Se houver uma locação pendente, exibe o diálogo
            showLocacaoPendenteDialog()
        }

        val botaoAlugar = findViewById<View>(R.id.containerRent)
        val botaoVerPontos = findViewById<View>(R.id.containerMap)

        val logoutButton = findViewById<ImageView>(R.id.btLogout)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, OpeningActivity::class.java))
        }

        // Define o listener de clique para o botão de início
        botaoAlugar.setOnClickListener {
            // Cria uma intenção para iniciar a atividade de locar armario
            val intent = Intent(this, LocationActivity::class.java)
            // Inicia a atividade de locação
            startActivity(intent)
        }

        botaoVerPontos.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("vindo_da_tela_usuário", true)
            startActivity(intent)
        }
    }

    private fun showLocacaoPendenteDialog() {
        // Aqui você cria e exibe o diálogo para avisar o usuário sobre a locação pendente
        val dialog = AlertDialog.Builder(this)
            .setTitle("Locação Pendente")
            .setMessage("Você tem uma locação pendente. Deseja continuar?")
            .setPositiveButton("Sim") { dialog, _ ->
                // Ação a ser executada quando o usuário clicar em "Sim"
                dialog.dismiss()
                // Coloque aqui o código para continuar com a locação pendente
                val intent = Intent(this, QRcodeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Não") { dialog, _ ->
                // Ação a ser executada quando o usuário clicar em "Não"
                dialog.dismiss()
                // Coloque aqui o código para cancelar a locação pendente
                cancelLocacaoPendente()
            }
            .setCancelable(false) // Impede que o usuário feche o diálogo ao tocar fora dele
            .create()

        dialog.show()
    }

    private fun cancelLocacaoPendente() {
        // Coloque aqui o código para cancelar a locação pendente
        // Por exemplo, você pode remover o status de locação pendente das SharedPreferences
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(qrCodeBitMapKey, false) // Define como false para cancelar a locação pendente
        editor.apply()
    }

}