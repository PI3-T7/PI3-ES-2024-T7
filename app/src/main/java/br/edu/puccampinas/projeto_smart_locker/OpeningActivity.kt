package br.edu.puccampinas.projeto_smart_locker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

/**
 * Esta classe representa a atividade de abertura do aplicativo.
 * Ela exibe um botão que leva à atividade de login e um TextView para cadastro.
 */
class OpeningActivity : AppCompatActivity() {
    override fun onStart() {
        super.onStart()
        verifyLoggedUser()
    }

    /**
     * Método chamado quando a atividade é criada.
     * Configura o layout e define o comportamento dos botões de início e cadastro.
     * @param savedInstanceState O estado da instância da atividade, se disponível.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opening)
        val botaoComecar = findViewById<Button>(R.id.bt_begin)
        val etCadastro = findViewById<TextView>(R.id.already)
        val imgButtonMap = findViewById<ImageButton>(R.id.imgButton_map)
        // Inicializa o botão de início

        // Define o listener de clique para o botão de início
        botaoComecar.setOnClickListener {
            // Cria uma intenção para iniciar a atividade de login
            val intent = Intent(this, SignUpActivity::class.java)
            // Inicia a atividade de login
            startActivity(intent)
        }
        // Define o listener de clique para o TextView de cadastro
        etCadastro.setOnClickListener {
            // Cria uma intenção para iniciar a atividade de cadastro
            val intent = Intent(this, LoginActivity::class.java)
            // Inicia a atividade de cadastro
            startActivity(intent)
        }
        // Define o listener de clique para o botão de início
        imgButtonMap.setOnClickListener {
            // Cria uma intenção para iniciar a atividade de login
            val intent = Intent(this, MapActivity::class.java)
            // Inicia a atividade de login
            startActivity(intent)
        }
    }
    private fun verifyLoggedUser() {
        var user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(Intent(this, ClientMainScreenActivity::class.java))
        }
    }
}
