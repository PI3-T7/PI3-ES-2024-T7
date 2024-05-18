package br.edu.puccampinas.projeto_smart_locker

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityCheckDataNfcBinding
import com.google.firebase.firestore.FirebaseFirestore

class CheckDataNfcActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCheckDataNfcBinding.inflate( layoutInflater ) }
    private val database by lazy { FirebaseFirestore.getInstance() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding){
            imgArrow.setOnClickListener { finish() }
            database.collection("Pessoas")
                .document(intent.getStringExtra("tag_data").toString())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Erro no Firebase Firestore", error.message ?: "Erro desconhecido")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        tvName.text = snapshot.get("nome_completo").toString()
                        tvPhone.text = snapshot.get("celular").toString()
                    }
                }
        }
    }
}
