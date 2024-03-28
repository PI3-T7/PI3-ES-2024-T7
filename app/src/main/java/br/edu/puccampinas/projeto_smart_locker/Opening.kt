package br.edu.puccampinas.projeto_smart_locker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button

/**
 * Esta classe representa a atividade de abertura do aplicativo.
 * Ela exibe um botão que leva à atividade de login.
 */
class Opening : AppCompatActivity() {

    // Botao para iniciar a atividade de login
    private lateinit var botaoComecar: Button

    /**
     * Método chamado quando a atividade é criada.
     * Configura o layout e define o comportamento do botão de início.
     * @param savedInstanceState O estado da instância da atividade, se disponível.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opening)

        // Inicializa o botão de início
        botaoComecar = findViewById(R.id.bt_begin)

        // Define o listener de clique para o botão de início
        botaoComecar.setOnClickListener {
            // Cria uma intenção para iniciar a atividade de login
            val intent = Intent(this, Login_activity::class.java)
            // Inicia a atividade de login
            startActivity(intent)
        }
    }
}