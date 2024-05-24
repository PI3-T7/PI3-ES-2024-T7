package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityManagerMainScreenBinding
import android.nfc.NfcAdapter
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity que representa a tela principal para gerentes.
 */
class ManagerMainScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManagerMainScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private var nfcAdapter: NfcAdapter? = null
    private val callback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            showLogoutDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Configurando o botão voltar para sair da conta do app
        this.onBackPressedDispatcher.addCallback(this, callback)

        // Configura um listener para alterações no documento do Firestore
        with(binding){
            database.collection("Pessoas")
                .document(auth.currentUser?.uid.toString())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Erro no Firebase Firestore", error.message ?: "Erro desconhecido")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        "Gerente ${snapshot.get("nome_completo")}".also { appCompatTextView3.text = it }
                    }
                }
            // Configura o botão de logout para desconectar o usuário e navegar para a atividade de abertura
            btLogout.setOnClickListener{
                showLogoutDialog()
            }
            // Configura o botão para ler tags NFC, verificando se o dispositivo suporta NFC
            viewReadNfc.setOnClickListener{
                // Verificando se o dispositivo possui NFC
                if (nfcAdapter == null) {
                    showErrorMessage()
                } else {
                    try {
                        startActivity(Intent(this@ManagerMainScreenActivity, ReadNfcActivity::class.java))
                    } catch (e: Exception) {
                        Log.e("NFCIntentError", "Erro ao iniciar ReadNfcActivity", e)
                    }
                }
            }

            // Configura o botão para liberar aluguel, navegando para a atividade de QR code
            viewReleaseRental.setOnClickListener{
                // Verificando se o dispositivo possui NFC, por enquanto deixar sem verificação
                if (nfcAdapter == null) {
                    showLogoutDialog()
                } else {
                    try {
                        startActivity(Intent(this@ManagerMainScreenActivity, QRcodeManagerActivity::class.java))
                    } catch (e: Exception) {
                        Log.e("NFCIntentError", "Erro ao iniciar ReadNfcActivity", e)
                    }
                }
            }
        }
    }

    /**
     * Exibe um diálogo de confirmação de cancelamento com opções "SIM" e "NÃO".
     * Este diálogo é usado para confirmar se o usuário deseja cancelar uma operação.
     * Dependendo da escolha do usuário, a atividade pode ser finalizada e outra atividade pode ser iniciada.
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
            customDialog.dismiss()
            auth.signOut()
            finish()
        }

        // Mostre o diálogo
        customDialog.show()
    }

    private fun showErrorMessage() {
        // Inflate o layout personalizado
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog_error, null)

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
            append("O dispositivo não possui tecnologia NFC!")
        }

        // Mostre o diálogo
        alertDialog.show()
    }
}
