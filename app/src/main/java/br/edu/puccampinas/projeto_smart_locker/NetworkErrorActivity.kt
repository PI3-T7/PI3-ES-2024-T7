package br.edu.puccampinas.projeto_smart_locker

import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityNetworkErrorBinding

class NetworkErrorActivity : AppCompatActivity() {

    private val binding by lazy {ActivityNetworkErrorBinding.inflate(layoutInflater)}
//    private val networkChecker by lazy {
//        NetworkChecker(
//            ContextCompat.getSystemService(this, ConnectivityManager::class.java)
//                ?: throw IllegalStateException("ConnectivityManager not available")
//        )
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnTryAgain.setOnClickListener{
            finish()
        }
    }
}