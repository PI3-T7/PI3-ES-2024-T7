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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityClientMainScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList
import java.util.Calendar

/**
 * Activity responsável pela tela principal/home do cliente.
 * @authors: Isabella e Lais.
 */
class ClientMainScreenActivity : AppCompatActivity() {
    // declaração/inicialização do layout, firebase e firestore
    private val binding by lazy { ActivityClientMainScreenBinding.inflate(layoutInflater) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance() }
    private val REQUEST_LOCATION_PERMISSION = 1001 // Código de solicitação para a permissão de localização
    private val callback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            showLogoutDialog()
        }
    }

    private val db = FirebaseFirestore.getInstance()
    /**
     * Método chamado quando a atividade é criada.
     * @authors: Lais.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configurando o botão voltar para sair da conta do app
        this.onBackPressedDispatcher.addCallback(this, callback)

        // Checando se tem locação pendente
        checkPendingRental()

        // Obtém o nome do usuário atual e exibe na tela
        database.collection("Pessoas")
            .document(auth.currentUser?.uid.toString()).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Erro no Firebase Firestore", error.message.toString())
                }
                if (snapshot != null && snapshot.exists()) {
                     binding.appCompatTextView3.text = buildString {
                         append("Olá, ")
                         append(snapshot.get("nome_completo").toString())
                     }
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

    /**
     * Exibe um diálogo de Locação Pendente customizado com uma mensagem simples e botões "SIM" e "NÃO".
     * Esse dialog tem uma função específica só para ele porque executa exclusivamente funções de
     * cancelar a pendência de locação.
     * @authors: Lais.
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
            clearPendingRental()
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
     * Esse dialog tem uma função específica só para ele porque executa exclusivamente funções de
     * logout do sistema.
     * @authors: Lais.
     */
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
            customDialog.dismiss()
            finish()
        }

        // Mostre o diálogo
        customDialog.show()
    }

    /**
     * Exibe um diálogo de AVISO customizado com uma mensagem simples e um botão "OK".
     * A mensagem a ser exibida no diálogo de alerta.
     * @authors: Lais.
     */
    private fun showAlertMessage() {
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
        textViewMessage.text = buildString {
            append("Aviso: Você precisa ter pelo menos um cartão cadastrado para alugar um armário.")
        }

        // Mostre o diálogo
        alertDialog.show()
    }

    /**
     * Verifica se o usuário tem pelo menos um cartão cadastrado antes de permitir alugar um armário.
     * @authors: Isabella.
     */
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
                            val cartoes = document.get("cartoes") as? ArrayList<*>

                            // Verifica se o array não é nulo ou vazio
                            if (!cartoes.isNullOrEmpty()) {
                                checkHour()
                            } else {
                                // Se não houver cartões, exibe uma mensagem ao usuário
                                showAlertMessage()
                            }
                        } else {
                            // Se não houver cartões, exibe uma mensagem ao usuário
                            showAlertMessage()
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
                // Se a permissão foi concedida, inicia a activity do mapa
                intent.putExtra("vindo_da_tela_usuario", true)
                startActivity(Intent(this@ClientMainScreenActivity, MapActivity::class.java))
            }
        }
    }

    /**
     * Verifica se o estabelecimento está aberto com base na hora atual.
     * @authors: Lais.
     */
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

    /**
     * Verifica se há uma locação pendente.
     * @authors: Lais.
     */
    private fun checkPendingRental() {
        val sharedPreferences = getSharedPreferences("SmartLockerPrefs", Context.MODE_PRIVATE)
        val pendingRental = sharedPreferences.getBoolean("pending_rental", false)
        if (pendingRental) {
            showLocacaoPendenteDialog()
        }
    }

    /**
     * Limpa o estado de locação pendente.
     * @authors: Lais.
     */
    private fun clearPendingRental() {
        val sharedPreferences = getSharedPreferences("SmartLockerPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("pending_rental", false)
        editor.apply()
    }
}
