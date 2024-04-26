package br.edu.puccampinas.projeto_smart_locker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityClientMainScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientMainScreenActivity : AppCompatActivity() {
    private val binding by lazy { ActivityClientMainScreenBinding.inflate( layoutInflater ) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance() }

    private val sharedPref = "Locacao"
    private val qrCodeBitMapKey = "locacaoPendente"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        database.collection("Pessoas")
            .document(auth.currentUser?.uid.toString()).addSnapshotListener {snapshot, error ->
                if (error != null) {
                    Log.e("Erro no Firebase Firestore", error.message.toString())
                }
                if (snapshot != null && snapshot.exists()) {
                    "Olá, ${snapshot.get("nome_completo").toString()}".also { binding.appCompatTextView3.text = it }
                }
            }

        with(binding) {
            btLogout.setOnClickListener {
                auth.signOut()
                finish()
            }
            containerMap.setOnClickListener {
                intent.putExtra("vindo_da_tela_usuário", true)
                startActivity(Intent(this@ClientMainScreenActivity, MapActivity::class.java))
            }
            containerCards.setOnClickListener {
                startActivity(Intent(this@ClientMainScreenActivity, CartoesActivity::class.java))
            }
            containerRent.setOnClickListener {
                startActivity(Intent(this@ClientMainScreenActivity, LocationActivity::class.java))
            }
        }

        // Verifica se há uma locação pendente
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val locacaoPendente = prefs.getBoolean(qrCodeBitMapKey, false)

        if (locacaoPendente) {
            // Se houver uma locação pendente, exibe o diálogo
            showLocacaoPendenteDialog()
        }
    }

    private fun showLocacaoPendenteDialog() {
        // Aqui você cria e exibe o diálogo para avisar o usuário sobre a locação pendente
        val dialog = AlertDialog.Builder(this)
            .setTitle("Locação Pendente")
            .setMessage("Você tem uma locação pendente. Deseja continuar?")
            .setPositiveButton("Sim") { dialog, _ ->
                // Ação a ser executada quando o usuário clicar em "Sim"
                dialog.dismiss()
                // Coloque aqui o código para continuar com a locação pendente
                val intent = Intent(this, QRcodeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Não") { dialog, _ ->
                // Ação a ser executada quando o usuário clicar em "Não"
                dialog.dismiss()
                // Coloque aqui o código para cancelar a locação pendente
                cancelLocacaoPendente()
            }
            .setCancelable(false) // Impede que o usuário feche o diálogo ao tocar fora dele
            .create()

        dialog.show()
    }

    private fun cancelLocacaoPendente() {
        // Coloque aqui o código para cancelar a locação pendente
        // Por exemplo, você pode remover o status de locação pendente das SharedPreferences
        val prefs = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(qrCodeBitMapKey, false) // Define como false para cancelar a locação pendente
        editor.apply()
    }
}