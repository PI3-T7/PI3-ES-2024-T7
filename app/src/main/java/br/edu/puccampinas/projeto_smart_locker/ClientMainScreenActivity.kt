package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityClientMainScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientMainScreenActivity : AppCompatActivity() {
    private val binding by lazy { ActivityClientMainScreenBinding.inflate( layoutInflater ) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val bd by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bd.collection("Pessoas")
            .document(auth.currentUser?.uid.toString())
            .get().addOnSuccessListener { document ->
                "Olá\n${document.getString("nome_completo")}".also { binding.name.text = it }
            }

        with(binding) {
            btLogout.setOnClickListener {
                auth.signOut()
                finish()
            }
            containerMap.setOnClickListener {
                startActivity(Intent(this@ClientMainScreenActivity, MapActivity::class.java))
            }
            containerCards.setOnClickListener {
                startActivity(Intent(this@ClientMainScreenActivity, CartoesActivity::class.java))
            }
            containerRent.setOnClickListener {
                startActivity(Intent(this@ClientMainScreenActivity, LocationActivity::class.java))
            }
        }
    }
}