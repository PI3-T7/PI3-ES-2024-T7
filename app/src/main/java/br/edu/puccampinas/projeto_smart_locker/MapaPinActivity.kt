package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MapaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_pin)

        val buttonRotas = findViewById<Button>(R.id.button_routes)
        buttonRotas.setOnClickListener {
            // Ir para tela de rotas
            val intent = Intent(this, RotaActivity::class.java)
            startActivity(intent)
            }
    }
}

