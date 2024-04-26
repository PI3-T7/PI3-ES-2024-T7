package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityOpeningBinding
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
class OpeningActivity : AppCompatActivity() {
    private val REQUEST_LOCATION_PERMISSION = 1001 // Defina um código de solicitação para a permissão de localização
    private val binding by lazy { ActivityOpeningBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding){
            imgBtnMap.setOnClickListener {
                // Quando o botão for clicado, solicite a permissão de localização
                requestLocationPermission()
            }
            btnBegin.setOnClickListener {
                startActivity(Intent(this@OpeningActivity, SignUpActivity::class.java))
            }
            btnAlready.setOnClickListener {
                startActivity(Intent(this@OpeningActivity, LoginActivity::class.java))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) startActivity(Intent(this, ClientMainScreenActivity::class.java))
    }

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Se a permissão foi concedida, abra a activity do mapa
                startActivity(Intent(this, MapActivity::class.java))
            }
        }
    }
}