package br.edu.puccampinas.projeto_smart_locker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityRentedLockerBinding

class RentedLockerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityRentedLockerBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}