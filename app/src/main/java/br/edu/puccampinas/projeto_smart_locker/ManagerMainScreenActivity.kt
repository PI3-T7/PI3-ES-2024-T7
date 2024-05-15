package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityManagerMainScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManagerMainScreenActivity : AppCompatActivity() {
    private val binding by lazy { ActivityManagerMainScreenBinding.inflate( layoutInflater ) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding){
            database.collection("Pessoas")
                .document(auth.currentUser?.uid.toString())
                .addSnapshotListener {snapshot, error ->
                    if (error != null) {
                        Log.e("Erro no Firebase Firestore", error.message.toString())
                    }
                    if (snapshot != null && snapshot.exists()) {
                        "Ola, gerente ${snapshot.get("nome_completo")}".also { appCompatTextView3.text = it }
                    }
                }
            btLogout.setOnClickListener{
                auth.signOut()
                finish()
            }
            viewReadNfc.setOnClickListener{
                startActivity(Intent(this@ManagerMainScreenActivity,ReadNfcActivity::class.java))
            }
            viewReleaseRental.setOnClickListener{
                startActivity(Intent(this@ManagerMainScreenActivity,OpenLockerActivity::class.java))
            }
        }

    }
}