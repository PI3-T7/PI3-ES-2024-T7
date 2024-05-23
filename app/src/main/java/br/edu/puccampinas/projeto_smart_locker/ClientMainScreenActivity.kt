package br.edu.puccampinas.projeto_smart_locker

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityClientMainScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap

class ClientMainScreenActivity : AppCompatActivity() {
    // declaração/inicialização do layout,firebase e firestore
    private val binding by lazy { ActivityClientMainScreenBinding.inflate(layoutInflater) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance() }
    // chaves para SharedPreferences
    private val sharedPref = "Locacao"
    private val qrCodeBitMapKey = "locacaoPendente"

    private val REQUEST_LOCATION_PERMISSION = 1001 // Código de solicitação para a permissão de localização

    private val db = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        database.collection("Pessoas")
            .document(auth.currentUser?.uid.toString()).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Erro no Firebase Firestore", error.message.toString())
                }
                if (snapshot != null && snapshot.exists()) {
                    "Olá, ${
                        snapshot.get("nome_completo").toString()
                    }".also { binding.appCompatTextView3.text = it }
                }
            }

        with(binding) {
            // Botão de logout
            btLogout.setOnClickListener {
                showLogoutDialog()
            }

            // Container do mapa
            containerMap.setOnClickListener {
                // Quando o botão for clicado, solicite a permissão de localização
                requestLocationPermission()
            }

            // Container dos cartões
            containerCards.setOnClickListener {
                startActivity(Intent(this@ClientMainScreenActivity, CardsActivity::class.java))
            }

            // Container de aluguel
            containerRent.setOnClickListener {
                verificarCartaoCadastrado()

            }
        }

    }

    override fun onResume() {
        super.onResume()

        // Verifica se há uma locação pendente
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val locacaoPendente = prefs.getBoolean(qrCodeBitMapKey, false)

        if (locacaoPendente) {
            // Se houver uma locação pendente, exibe o diálogo
            showLocacaoPendenteDialog()
        }
    }

    override fun onStart() {
        super.onStart()

        // Verifica se há uma locação pendente
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val locacaoPendente = prefs.getBoolean(qrCodeBitMapKey, false)

        if (locacaoPendente) {
            // Se houver uma locação pendente, exibe o diálogo
            showLocacaoPendenteDialog()
        }
    }

    /**
     * Exibe um diálogo de Locação Pendente customizado com uma mensagem simples e botões "SIM" e "NÃO".
     * Esse dialog tem uma função específica só para ele porque executa excluivamente funções de
     * cancelar a pendência de locação
     */

    private fun showLocacaoPendenteDialog() {
        // Inflar o layout do diálogo personalizado
        val dialogView1 = layoutInflater.inflate(R.layout.custom_dialog_pending_rental, null)

        val customDialog1 = AlertDialog.Builder(this)
            .setView(dialogView1)
            .setCancelable(false) // Impede que o usuário feche o diálogo ao tocar fora dele
            .create()

        // Defina a altura desejada para o diálogo
        customDialog1.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 600)

        // Configure os botões do diálogo
        val btnNo1 = dialogView1.findViewById<Button>(R.id.btnNo1)
        val btnYes1 = dialogView1.findViewById<Button>(R.id.btnYes1)

        btnNo1.setOnClickListener {
            // Ação a ser executada quando o usuário clicar em "Não"
            // Fecha o diálogo
            customDialog1.dismiss()
            // Cancela a locação pendente
            cancelLocacaoPendente()
        }

        btnYes1.setOnClickListener {
            // Ação a ser executada quando o usuário clicar em "Sim"
            customDialog1.dismiss()
            // Coloque aqui o código para continuar com a locação pendente
            val intent = Intent(this, QRcodeActivity::class.java)
            startActivity(intent)
            finish()
        }

        customDialog1.show()
    }

    /**
     * Exibe um diálogo de Logout customizado com uma mensagem simples e botões "SIM" e "NÃO".
     * Esse dialog tem uma função específica só para ele porque executa excluivamente funções de
     * logout do sistema.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showLogoutDialog() {
        // Inflate o layout customizado
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_logout, null)

        // Crie o AlertDialog e ajuste sua altura desejada
        val customDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Impede o fechamento do diálogo ao tocar fora dele
            .create()

        // Defina a altura desejada para o diálogo
        customDialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 600)

        // Configure os botões do diálogo
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo)
        val btnYes = dialogView.findViewById<Button>(R.id.btnYes)

        btnNo.setOnClickListener {
            // Fecha o diálogo sem fazer logout
            customDialog.dismiss()
        }

        btnYes.setOnClickListener {
            // Realize o logout
            auth.signOut()
            startActivity(Intent(this,OpeningActivity::class.java))
            finish()
            customDialog.dismiss()
        }

        // Mostre o diálogo
        customDialog.show()
    }

    /**
     * Exibe um diálogo de AVISO customizado com uma mensagem simples e um botão "OK".
     * @param message A mensagem a ser exibida no diálogo de alerta.
     */

    private fun showAlertMessage(message: String) {
        // Inflate o layout personalizado
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog_warning, null)

        // Crie o AlertDialog com o layout personalizado
        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        // Configure o botão OK para fechar o diálogo
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }

        // Atualize a mensagem no TextView
        val textViewMessage = view.findViewById<TextView>(R.id.tvMessage)
        textViewMessage.text = message

        // Mostre o diálogo
        alertDialog.show()
    }

    private fun cancelLocacaoPendente() {
        // Cancelando locação pendente
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(
            qrCodeBitMapKey,
            false
        ) // Define como false para cancelar a locação pendente
        editor.apply()
    }

    private fun verificarCartaoCadastrado() {
        // Pegando o ID do usuário logado
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId != null) {
            db.collection("Pessoas").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Verifica se o campo "cartoes" existe no documento
                        if (document.contains("cartoes")) {
                            // Obtém o array de cartões do documento
                            val cartoes = document.get("cartoes") as? ArrayList<HashMap<String, String>>

                            // Verifica se o array não é nulo ou vazio
                            if (!cartoes.isNullOrEmpty()) {
                                checkHour()
                            } else {
                                // Se não houver cartões, exibe uma mensagem ao usuário
                                showAlertMessage("Aviso: Você precisa ter pelo menos um cartão cadastrado para alugar um armário.")
                            }
                        } else {
                            // Se não houver cartões, exibe uma mensagem ao usuário
                            showAlertMessage("Aviso: Você precisa ter pelo menos um cartão cadastrado para alugar um armário.")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Em caso de falha, exibe uma mensagem de erro ao usuário
                    Toast.makeText(
                        this@ClientMainScreenActivity,
                        "Erro ao acessar documento: $exception",
                        Toast.LENGTH_SHORT
                    ).show()
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
                intent.putExtra("vindo_da_tela_usuario", true)
                startActivity(Intent(this@ClientMainScreenActivity, MapActivity::class.java))
            }
        }
    }

    private fun checkHour() {
        // Obter a hora atual
        val calendario = Calendar.getInstance()
        val horaAtual: Int =
            calendario.get(Calendar.HOUR_OF_DAY)  // Obtém a hora atual como um inteiro
        val minutoAtual: Int = calendario.get(Calendar.MINUTE)      // Obtém os minutos atuais

        // Converte os valores para Double antes de realizar a operação de divisão
        val hora: Double = horaAtual.toDouble() + minutoAtual.toDouble() / 60.0

        // Verificar se está entre 7 e 8 horas
        if (hora < 7 || hora > 17.5) {
            val intent2 = Intent(this, ClosedEstablishmentActivity::class.java)
            startActivity(intent2)
        } else {
            // Se for entre 7h e 17h30:
            startActivity(Intent(this@ClientMainScreenActivity, LocationActivity::class.java))
        }
    }

}
