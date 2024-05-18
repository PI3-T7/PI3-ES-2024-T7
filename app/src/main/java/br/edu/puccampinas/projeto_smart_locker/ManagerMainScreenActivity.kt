package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityManagerMainScreenBinding
import android.nfc.NfcAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManagerMainScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManagerMainScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        with(binding){
            database.collection("Pessoas")
                .document(auth.currentUser?.uid.toString())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Erro no Firebase Firestore", error.message ?: "Erro desconhecido")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        appCompatTextView3.text = "Olá, gerente ${snapshot.get("nome_completo")}"
                    }
                }
            btLogout.setOnClickListener{
                auth.signOut()
                finish()
            }
            viewReadNfc.setOnClickListener{
                // Verificando se o dispositivo possui NFC
                if (nfcAdapter == null) {
                    Toast.makeText(this@ManagerMainScreenActivity, "O dispositivo não possui tecnologia NFC!", Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        startActivity(Intent(this@ManagerMainScreenActivity, ReadNfcActivity::class.java))
                    } catch (e: Exception) {
                        Log.e("NFCIntentError", "Erro ao iniciar ReadNfcActivity", e)
                    }
                }
            }
            viewReleaseRental.setOnClickListener{
                startActivity(Intent(this@ManagerMainScreenActivity, OpenLockerActivity::class.java))
            }
        }
    }
}
