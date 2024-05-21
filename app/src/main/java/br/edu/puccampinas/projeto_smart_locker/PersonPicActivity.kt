package br.edu.puccampinas.projeto_smart_locker

import android.content.ContentValues.TAG
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
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

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

        Log.d(TAG, "numPessoas: $numPessoas")
        Log.d(TAG, "fotosTiradas: $fotosTiradas")
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
                // obtendo data e hora do sistema para cadastrar a locação
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.getDefault())
                val timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault())

                val dataLocacao = dateFormat.format(calendar.time)
                val horaLocacao = timeFormat.format(calendar.time)

                obterNumeroArmarioLivre { numeroArmario ->
                    numeroArmario?.let {
                        // Número do armário livre disponível
                        val locacaoData = LocacaoData(
                            status = true,
                            uidUsuario = dadosCliente.id,
                            uidUnidade = dadosCliente.unidade,
                            numeroArmario = it,
                            tempoEscolhido = dadosCliente.opcao,
                            preco = dadosCliente.preco,
                            dataLocacao = dataLocacao,
                            horaLocacao = horaLocacao,
                            caucao = 0.0
                        )
                        uploadPhotosToFirebase(listOf(imagePath), dadosCliente.id, locacaoData)
                    } ?: run {
                        // Nenhum armário livre encontrado, trate de acordo com sua lógica de negócios
                        Log.e("Firestore", "Nenhum armário livre encontrado.")
                        Toast.makeText(this, "Nenhum armário livre encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.imgArrow.setOnClickListener {
            val intent = Intent(this, SelectPeopleNumActivity::class.java)
            startActivity(intent)
        }
    }

    private fun uploadPhotosToFirebase(
        imagePaths: List<String>,
        clientId: String,
        locacaoData: LocacaoData
    ) {
        val photoUrls = mutableListOf<String>()

        // Realiza o upload de cada foto individualmente
        imagePaths.forEachIndexed { index, imagePath ->
            val file = File(imagePath)
            val storageRef = storage.reference
            val clientPhotoRef = storageRef.child("clients/$clientId/${file.name}")

            val uri = Uri.fromFile(file)
            val uploadTask = clientPhotoRef.putFile(uri)

            uploadTask.addOnSuccessListener { uploadTaskSnapshot ->
                clientPhotoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    photoUrls.add(downloadUrl.toString())

                    // Quando todas as fotos forem enviadas com sucesso, salva os dados da locação
                    if (photoUrls.size == imagePaths.size) {
                        saveLocacaoToFirestore(photoUrls, locacaoData)
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("FirebaseStorage", "Erro ao fazer upload da foto $index: ${e.message}", e)
            }
        }
    }

    private fun saveLocacaoToFirestore(
        photoUrls: List<String>,
        locacaoData: LocacaoData
    ) {
        val locacao = hashMapOf(
            "status" to locacaoData.status,
            "uid_usuario" to locacaoData.uidUsuario,
            "uid_unidade" to locacaoData.uidUnidade,
            "numero_armario" to locacaoData.numeroArmario,
            "tempo_escolhido" to locacaoData.tempoEscolhido,
            "preco" to locacaoData.preco,
            "fotos" to photoUrls,
            "data_locacao" to locacaoData.dataLocacao,
            "hora_locacao" to locacaoData.horaLocacao,
            "caucao" to locacaoData.caucao
        )

        firestore.collection("Locação")
            .add(locacao)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Locação salva com ID: ${documentReference.id}")
                updateArmarioStatus(locacaoData.numeroArmario.toInt())
                val intent = Intent(this, ClosetReleasedActivity::class.java)
                val dadosJson = Gson().toJson(dadosCliente)
                intent.putExtra("dadosCliente", dadosJson)
                startActivity(intent)
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Erro ao salvar locação no Firestore: ${e.message}", e)
            }
    }



    private fun obterNumeroArmarioLivre(callback: (String?) -> Unit) {
        val nomeDocumento = dadosCliente.unidade
        firestore.collection("Unidades de Locação")
            .document(nomeDocumento)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val lockers = documentSnapshot.data?.get("lockers") as Map<String, Boolean>?

                // Filtrar os armários livres
                val armariosLivres = lockers?.filterValues { it }

                // Ordenar as chaves do Map (números dos armários)
                val armariosOrdenados = armariosLivres?.keys?.sorted()

                // Obter o primeiro armário livre (menor número)
                val primeiroArmarioLivre = armariosOrdenados?.firstOrNull()

                callback(primeiroArmarioLivre)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erro ao obter a coleção 'Unidades de Locação': ${e.message}", e)
                callback(null)
            }
    }


    private fun updateArmarioStatus(numeroArmario: Int) {
        val nomeDocumento = dadosCliente.unidade
        val lockersCollection = FirebaseFirestore.getInstance().collection("Unidades de Locação")
        val lockerDocument = lockersCollection.document(nomeDocumento)

        lockerDocument.update("lockers.$numeroArmario", false)
            .addOnSuccessListener {
                // Atualização bem-sucedida
                Log.d(TAG, "Status do armário $numeroArmario atualizado para false")
            }
            .addOnFailureListener { e ->
                // Falha ao atualizar o status do armário
                Log.e(TAG, "Erro ao atualizar status do armário $numeroArmario: $e")
            }
    }
}

data class LocacaoData(
    val status: Boolean,
    val uidUsuario: String,
    val uidUnidade: String,
    val numeroArmario: String,
    val tempoEscolhido: String,
    val preco: Double,
    val dataLocacao: String,
    val horaLocacao: String,
    val caucao: Double
)
