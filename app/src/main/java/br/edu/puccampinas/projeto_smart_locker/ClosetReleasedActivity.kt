package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityClosetReleasedBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class ClosetReleasedActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvUnit: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvStartLocation: TextView
    private lateinit var tvEndLocation: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvPrice: TextView
    private val db = FirebaseFirestore.getInstance()
    private lateinit var dadosCliente: DadosCliente
    private val binding by lazy {ActivityClosetReleasedBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonHome2.setOnClickListener{
            startActivity(Intent(this,ManagerMainScreenActivity::class.java))
            finish()
        }
        // Inicializa as views
        imageView = findViewById(R.id.img_user)
        tvName = findViewById(R.id.tv_name)
        tvPhone = findViewById(R.id.tv_phone)
        tvUnit = findViewById(R.id.tv_unit)
        tvAddress = findViewById(R.id.tv_address)
        tvStartLocation = findViewById(R.id.tv_start_location)
        tvEndLocation = findViewById(R.id.tv_end_location)
        tvTime = findViewById(R.id.tv_time)
        tvPrice = findViewById(R.id.tv_price)

        // Obtém os dados do cliente da Intent
        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)

        // Carrega os dados do usuário utilizando o ID do cliente
        loadUserData(dadosCliente.id)
    }

    // Função para carregar os dados do usuário do Firestore
    private fun loadUserData(clientId: String) {
        val clientRef = db.collection("clients").document(clientId)
        clientRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Define os valores das TextViews com os dados do usuário
                    tvName.text = document.getString("name")
                    tvPhone.text = document.getString("phone")
                    tvUnit.text = document.getString("unit")
                    tvAddress.text = document.getString("address")
                    tvStartLocation.text = document.getString("startLocation")
                    tvEndLocation.text = document.getString("endLocation")
                    tvTime.text = document.getString("time")
                    tvPrice.text = document.getString("price")

                    val imageUri = document.getString("photoUrl")
                    Glide.with(this)
                        .load(imageUri)
                        .into(imageView)
                } else {
                    Toast.makeText(this, "Documento não encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
    }
}
