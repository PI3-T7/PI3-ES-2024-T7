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

/**
 * Activity responsável pela tela de abertura do aplicativo.
 * @RequiresApi(Build.VERSION_CODES.O) - Necessário para a funcionalidade no Android O e superior.
 * @authors: Isabella e Marcos.
 */
@RequiresApi(Build.VERSION_CODES.O)
class OpeningActivity : AppCompatActivity() {
    private val REQUEST_LOCATION_PERMISSION = 1001 // Código de solicitação para a permissão de localização
    private val binding by lazy { ActivityOpeningBinding.inflate(layoutInflater) }

    // Inicialização de uma instância de NetworkChecker para verificar a conectividade de rede.
    private val networkChecker by lazy {
        NetworkChecker(
            ContextCompat.getSystemService(this, ConnectivityManager::class.java)
                ?: throw IllegalStateException("ConnectivityManager not available")
        )
    }

    /**
     * Método onCreate é chamado quando a Activity é criada.
     * Aqui são configurados os clickListeners dos botões da tela inicial, direcionando para as respectivas telas.
     * @authors: Marcos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            imgBtnMap.setOnClickListener {
                // Verifica se há conexão com a internet antes de abrir a tela do mapa
                if (networkChecker.hasInternet()) {
                    // Quando o botão for clicado, solicita a permissão de localização
                    requestLocationPermission()
                } else {
                    startActivity(Intent(this@OpeningActivity, NetworkErrorActivity::class.java))
                }
            }
            // botão para para ir para tela de SignUp
            btnBegin.setOnClickListener {
                startActivity(Intent(this@OpeningActivity, SignUpActivity::class.java))
            }
            // botão para ir para tela de login
            btnAlready.setOnClickListener {
                startActivity(Intent(this@OpeningActivity, LoginActivity::class.java))
            }
        }
    }

    /**
     * Método onStart é chamado quando a Activity está prestes a se tornar visível.
     * Leva o usuário para a tela principal do app caso ele já esteja logado.
     * @authors: Marcos.
     */
    override fun onStart() {
        super.onStart()
        // Obtém o usuário atualmente logado através da instância do FirebaseAuth.
        val user = FirebaseAuth.getInstance().currentUser
        // Verifica se o usuário não é nulo
        if (user != null) {
            FirebaseFirestore.getInstance()
                .collection("Pessoas")
                // Obtém o documento associado ao ID do usuário logado.
                .document(user.uid).get()
                // Adiciona um listener para tratar o sucesso.
                .addOnSuccessListener { snapshot ->
                    // Verifica se o snapshot do documento não é nulo e se o documento existe.
                    if (snapshot != null && snapshot.exists()) {
                        // Verifica se o campo "gerente" no documento é "true".
                        if (snapshot.get("gerente").toString() == "true") {
                            // Se o usuário for um gerente, inicia a ManagerMainScreenActivity.
                            startActivity(Intent(this, ManagerMainScreenActivity::class.java))
                            // Encerra a OpeningActivity para que o usuário não possa voltar a ela pressionando o botão de voltar.
                            finish()
                        } else {
                            // Se o usuário não for um gerente, inicia a ClientMainScreenActivity.
                            startActivity(Intent(this, ClientMainScreenActivity::class.java))
                            finish()
                        }
                    }
                }
                // Adiciona um listener para tratar falhas
                .addOnFailureListener { error ->
                    // mensagem de erro se ocorrer uma falha ao acessar o Firestore.
                    Log.e("Erro no Firebase Firestore", error.message.toString())
                }
        }
    }

    /**
     * Solicita permissão de localização ao usuário.
     * @authors: Isabella.
     */
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

    /**
     * Função chamada quando a resposta à solicitação de permissão é recebida.
     * @param requestCode Código de solicitação da permissão.
     * @param permissions Lista de permissões solicitadas.
     * @param grantResults Resultados das permissões concedidas ou negadas.
     * @authors: Isabella.
     */
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
