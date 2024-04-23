package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityOpeningBinding
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
// Activity Opening -> Activity da tela de entrada caso o usuário não esteja logado ou não possua conta
class OpeningActivity : AppCompatActivity() {
    // Configuração do ViewBinding
    private val binding by lazy { ActivityOpeningBinding.inflate( layoutInflater ) }

    // No onCreate serão colocados os clickListeners dos botões da tela inicial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding){
            imgBtnMap.setOnClickListener {
                startActivity(Intent(this@OpeningActivity, MapActivity::class.java))
            }
            btnBegin.setOnClickListener {
                startActivity(Intent(this@OpeningActivity, SignUpActivity::class.java))
            }
            btnAlready.setOnClickListener {
                startActivity(Intent(this@OpeningActivity, LoginActivity::class.java))
            }
        }
    }

    // O onStart levará o usuário para a tela principal do app caso ele já esteja logado
    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) startActivity(Intent(this, ClientMainScreenActivity::class.java))
    }
}
