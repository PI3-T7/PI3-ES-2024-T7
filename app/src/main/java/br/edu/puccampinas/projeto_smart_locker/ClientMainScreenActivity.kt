package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth

class ClientMainScreenActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_main_screen)
        val logoutButton = findViewById<ImageView>(R.id.btLogout)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, OpeningActivity::class.java))
        }
    }
}