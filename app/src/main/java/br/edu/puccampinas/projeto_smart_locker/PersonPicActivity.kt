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

class PersonPicActivity: AppCompatActivity() {

    private lateinit var binding: ActivityPersonPicBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dadosCliente: DadosCliente
    private lateinit var imagePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonPicBinding.inflate(layoutInflater) // Infla o layout da atividade
        setContentView(binding.root) // Define o layout da atividade como o layout inflado

        storage = FirebaseStorage.getInstance() // Inicializa o Firebase Storage
        firestore = FirebaseFirestore.getInstance() // Inicializa o Firestore

        // Obtém os dados do cliente da Intent
        val dadosJson = intent.getStringExtra("dadosCliente")
        dadosCliente = Gson().fromJson(dadosJson, DadosCliente::class.java)
        imagePath = intent.getStringExtra("image_path") ?: "" // Obtém o caminho da imagem da Intent

        Log.d("PersonPicActivity", "Caminho da imagem: $imagePath")

        if (imagePath.isNotEmpty()) { // Verifica se o caminho da imagem não está vazio
            val imageFile = File(imagePath)
            if (imageFile.exists()) { // Verifica se o arquivo de imagem existe
                val imageView: ImageView = findViewById(R.id.picture_person)
                Glide.with(this)
                    .load(imagePath) // Carrega a imagem utilizando a biblioteca Glide
                    .into(imageView)
            } else {
                // Se o arquivo não for encontrado, exibe uma mensagem de erro e finaliza a atividade
                Log.e("PersonPicActivity", "O arquivo não foi encontrado no caminho especificado.")
                Toast.makeText(this, "O arquivo não foi encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            // Se o caminho da imagem não for fornecido, exibe uma mensagem de erro e finaliza a atividade
            Log.e("PersonPicActivity", "Caminho da imagem não fornecido.")
            Toast.makeText(this, "Caminho da imagem não encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Configura o clique do botão de finalização para fazer o upload da foto para o Firebase
        binding.buttonFinish.setOnClickListener {
            uploadPhotoToFirebase(imagePath, dadosCliente.id)
        }

        // Configura o clique na seta de voltar para retornar à atividade anterior
        binding.imgArrow.setOnClickListener {
            finish()
        }
    }

    // Função para fazer o upload da foto para o Firebase Storage
    private fun uploadPhotoToFirebase(imagePath: String, clientId: String) {
        val file = File(imagePath)
        val storageRef = storage.reference
        val clientPhotoRef = storageRef.child("clients/$clientId/${file.name}")

        val uri = Uri.fromFile(file)
        val uploadTask = clientPhotoRef.putFile(uri)

        // Adiciona um listener de sucesso e falha ao upload da foto
        uploadTask.addOnSuccessListener {
            clientPhotoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                // Após o upload bem-sucedido, salva a URL da foto no Firestore
                savePhotoUrlToFirestore(clientId, downloadUrl.toString())
            }
        }.addOnFailureListener {
            Log.e("FirebaseStorage", "Erro ao fazer upload da foto: ${it.message}", it)
        }
    }

    // Função para salvar a URL da foto no Firestore
    private fun savePhotoUrlToFirestore(clientId: String, photoUrl: String) {
        val clientRef = firestore.collection("clients").document(clientId)
        clientRef.update("photoUrl", photoUrl).addOnSuccessListener {
            // Após salvar a URL da foto com sucesso, inicia a próxima atividade
            val intent = Intent(this, ClosetReleasedActivity::class.java)
            val dadosJson = Gson().toJson(dadosCliente)
            intent.putExtra("dadosCliente", dadosJson)
            startActivity(intent)
        }.addOnFailureListener {
            Log.e("Firestore", "Erro ao salvar URL da foto no Firestore: ${it.message}", it)
        }
    }
}
