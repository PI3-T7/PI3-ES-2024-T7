package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityPersonPicBinding
import com.google.gson.Gson

class PersonPicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonPicBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dadosCliente: DadosCliente
    private lateinit var imagePath: String
    private var numPessoas: Int = 1
    private var fotosTiradas: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonPicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)
        imagePath = intent.getStringExtra("image_path") ?: ""
        numPessoas = intent.getIntExtra("numPessoas", 1)
        fotosTiradas = intent.getIntExtra("fotosTiradas", 0)

        Log.d("PersonPicActivity", "Caminho da imagem: $imagePath")

        if (imagePath.isNotEmpty()) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val imageView: ImageView = findViewById(R.id.picture_person)
                Glide.with(this)
                    .load(imagePath)
                    .into(imageView)
            } else {
                Log.e("PersonPicActivity", "O arquivo não foi encontrado no caminho especificado.")
                Toast.makeText(this, "O arquivo não foi encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Log.e("PersonPicActivity", "Caminho da imagem não fornecido.")
            Toast.makeText(this, "Caminho da imagem não encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.buttonFinish.setOnClickListener {
            if (fotosTiradas < numPessoas) {
                val intent = Intent(this, TakePicActivity::class.java)
                val dadosJson = Gson().toJson(dadosCliente)
                intent.putExtra("dadosCliente", dadosJson)
                intent.putExtra("numPessoas", numPessoas)
                intent.putExtra("fotosTiradas", fotosTiradas)
                startActivity(intent)
            } else {
                uploadPhotoToFirebase(imagePath, dadosCliente.id)
            }
        }

        binding.imgArrow.setOnClickListener {
            val intent = Intent(this, SelectPeopleNumActivity::class.java)
            startActivity(intent)
        }
    }

    private fun uploadPhotoToFirebase(imagePath: String, clientId: String) {
        val file = File(imagePath)
        val storageRef = storage.reference
        val clientPhotoRef = storageRef.child("clients/$clientId/${file.name}")

        val uri = Uri.fromFile(file)
        val uploadTask = clientPhotoRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            clientPhotoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                savePhotoUrlToFirestore(clientId, downloadUrl.toString())
            }
        }.addOnFailureListener {
            Log.e("FirebaseStorage", "Erro ao fazer upload da foto: ${it.message}", it)
        }
    }

    private fun savePhotoUrlToFirestore(clientId: String, photoUrl: String) {
        val clientRef = firestore.collection("clients").document(clientId)
        clientRef.update("photoUrl", photoUrl).addOnSuccessListener {
            val intent = Intent(this, ClosetReleasedActivity::class.java)
            val dadosJson = Gson().toJson(dadosCliente)
            intent.putExtra("dadosCliente", dadosJson)
            startActivity(intent)
        }.addOnFailureListener {
            Log.e("Firestore", "Erro ao salvar URL da foto no Firestore: ${it.message}", it)
        }
    }
}
