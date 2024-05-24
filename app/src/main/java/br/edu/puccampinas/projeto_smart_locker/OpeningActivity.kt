package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityOpeningBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@RequiresApi(Build.VERSION_CODES.O)
class OpeningActivity : AppCompatActivity() {
    private val REQUEST_LOCATION_PERMISSION =
        1001 // Defina um código de solicitação para a permissão de localização
    private val binding by lazy { ActivityOpeningBinding.inflate(layoutInflater) }

    // Inicialização de uma instancia de NetworkChecker para verificar a conectividad de rede.
    private val networkChecker by lazy {
        NetworkChecker(
            ContextCompat.getSystemService(this, ConnectivityManager::class.java)
                ?: throw IllegalStateException("ConnectivityManager not available")
        )
    }

    // no onCreate serão colocados os clickListeners dos botões da tela inicial
    // levando para as respectivas telas
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            imgBtnMap.setOnClickListener {
                // Verifica se há conexão com a internet antes de abrir a tela do mapa
                if (networkChecker.hasInternet()) {
                    // Quando o botão for clicado, solicite a permissão de localização
                    requestLocationPermission()
                } else {
                    startActivity(Intent(this@OpeningActivity, NetworkErrorActivity::class.java))
                }
            }
            btnBegin.setOnClickListener {
                startActivity(Intent(this@OpeningActivity, SignUpActivity::class.java))
            }
            btnAlready.setOnClickListener {
                startActivity(Intent(this@OpeningActivity, LoginActivity::class.java))
            }
        }
    }

    // o onStart levará o usuário para a tela principal do app caso ele já esteja logado
    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance()
                .collection("Pessoas")
                .document(user.uid).get().addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        if (snapshot.get("gerente").toString() == "true") {
                            startActivity(Intent(this, ManagerMainScreenActivity::class.java))
                            finish()
                        } else {
                            startActivity(Intent(this, ClientMainScreenActivity::class.java))
                            finish()
                        }
                    }
                }
                .addOnFailureListener { error ->
                    Log.e("Erro no Firebase Firestore", error.message.toString())
                }
        }
    }

    // Função responsável por solicitar permissão de localização
    private fun requestLocationPermission() {
        // Verifica se a versão do Android é igual ou superior a Marshmallow (API 23)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Solicita permissão de acesso à localização fina
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    // Função chamada quando a resposta à solicitação de permissão é recebida
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Chama a implementação padrão do método
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Verifica se o código da solicitação de permissão é para localização
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // Verifica se a permissão foi concedida
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Se a permissão foi concedida, inicie a activity do mapa
                startActivity(Intent(this, MapActivity::class.java))
            }
        }
    }
}
