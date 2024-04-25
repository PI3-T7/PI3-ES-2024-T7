package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth

class ClientMainScreenActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_main_screen)

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
}