package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityManagerMainScreenBinding
import android.nfc.NfcAdapter
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManagerMainScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManagerMainScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private var nfcAdapter: NfcAdapter? = null

    @RequiresApi(Build.VERSION_CODES.O)
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
                        "Olá, gerente ${snapshot.get("nome_completo")}".also { appCompatTextView3.text = it }
                    }
                }
            btLogout.setOnClickListener{
                auth.signOut()
                startActivity(Intent(this@ManagerMainScreenActivity,OpeningActivity::class.java))
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
                // Verificando se o dispositivo possui NFC
                if (nfcAdapter == null) {
                    Toast.makeText(this@ManagerMainScreenActivity, "O dispositivo não possui tecnologia NFC!", Toast.LENGTH_SHORT).show()
                } else {
                    try {
//                        startActivity(Intent(this@ManagerMainScreenActivity, QRcodeManagerActivity::class.java))
//                        startActivity(Intent(this@ManagerMainScreenActivity, WriteNfcActivity::class.java).putExtra("location_data", "valor da tag"))
                        startActivity(Intent(this@ManagerMainScreenActivity, EraseNfcActivity::class.java))
                    } catch (e: Exception) {
                        Log.e("NFCIntentError", "Erro ao iniciar ReadNfcActivity", e)
                    }
                }
            }
        }
    }
}
