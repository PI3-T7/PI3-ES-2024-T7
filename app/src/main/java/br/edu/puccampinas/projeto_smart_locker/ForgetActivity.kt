package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityForgetBinding
/**
 * Activity responsável pela página de esqueceu a senha -> envio de email para recuperação.
 * @authors: Marcos.
 */
class ForgetActivity : AppCompatActivity() {
    // declaração e inicialização do layout usando ViewBinding
    private val binding by lazy { ActivityForgetBinding.inflate( layoutInflater ) }
    /**
     * Método chamado quando a atividade é criada.
     * @authors: Marcos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // define o texto do TextView com o email recebido da Intent
        binding.appCompatTextView1.text = intent.extras?.getString("email")
        // configura o clique no botão "Ok"
        binding.butOk.setOnClickListener {
            // inicia a LoginActivity e finaliza a ForgetActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}