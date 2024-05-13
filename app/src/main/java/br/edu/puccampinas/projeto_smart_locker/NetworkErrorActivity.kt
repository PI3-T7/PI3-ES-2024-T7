package br.edu.puccampinas.projeto_smart_locker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityNetworkErrorBinding

/**
 * Classe NetworkErrorActivity representa uma Activity que é exibida quando ocorre um erro de rede.
 * Esta Activity fornece ao usuário a opção de tentar novamente (por meio do botão "Tentar Novamente").
 * @author isabellatressino
 */
class NetworkErrorActivity : AppCompatActivity() {

    private val binding by lazy { ActivityNetworkErrorBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnTryAgain.setOnClickListener {
            finish()
        }
    }
}