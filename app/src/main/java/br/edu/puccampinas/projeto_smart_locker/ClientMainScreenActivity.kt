package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth

class ClientMainScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_main_screen)
        val logoutButton = findViewById<ImageView>(R.id.btLogout)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, OpeningActivity::class.java))
        }


        // apenas para teste, pode apagar depois
        val btnCartoes = findViewById<View>(R.id.containerCards)
        btnCartoes.setOnClickListener{
            val intent = Intent(this, CartoesActivity::class.java)
            startActivity(intent)
        }
    }
}