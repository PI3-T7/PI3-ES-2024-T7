package br.edu.puccampinas.projeto_smart_locker

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.util.*
import kotlin.math.*

class LocationActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLocationBinding.inflate(layoutInflater) }

    // Variável que acessa os serviços de localização da API
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var latitudeUser: Double = 0.0
    private var longitudeUser: Double = 0.0

    companion object {
        // Código de solicitação de permissão
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("meuFiltro"))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verifique e solicite permissão de acesso à localização
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            // Permissão já concedida, obter a localização atual
            obterLocalizacaoAtual()
        }

        binding.buttonHome1.setOnClickListener {
            finish()
        }

        binding.buttonVoltar1.setOnClickListener {
            finish()
        }

        // Evento do botão que confirma a locação e chama a Activity para gerar o QRcode
        binding.btConfirmLocation.setOnClickListener {
            mandarDadosQrcode()
        }
    }

    override fun onStart() {
        super.onStart()
        // Atualiza o layout depois de verificar a hora
        checkHour()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obterLocalizacaoAtual()
            } else {
                // Permissão negada, mostrar mensagem ou tratar de outra forma
                Toast.makeText(
                    this,
                    "Permissão de localização negada.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun mandarDadosQrcode(){
        // Criando o objeto dados apenas para testar a passagem de dados para o QRcode
        // A classe DadosCliente está no final do código
        val dados = DadosCliente("", "", "", "", "", "", 0.0)

        // Verifica qual RadioButton está selecionado e atribui a opção correspondente ao objeto 'dados'
        val selectedOption = when {
            binding.btn30min.isChecked -> "30 minutos"
            binding.btn1hour.isChecked -> "1 hora"
            binding.btn2hours.isChecked -> "2 horas"
            binding.btn4hours.isChecked -> "4 horas"
            binding.btnUntil18.isChecked -> "Ate 18 horas"
            else -> ""
        }

        Log.d(TAG, "Selected Option: $selectedOption")

        val sharedPreferences = getSharedPreferences("uid", MODE_PRIVATE)
        val valorRecuperado = sharedPreferences.getString("uid", null)
        // Recuperando o id do usuário logado no momento
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        // Utilizando o id do usuário para buscar seu nome e celular, para passar junto com os
        // outros dados no QRCode.
        userId?.let {
            val userDocRef = db.collection("Pessoas").document(it)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("nome_completo")
                        val userPhone = document.getString("celular")
                        if (userName != null && userPhone != null) {
                            Log.d(TAG, "Nome do usuário: $userName, Celular: $userPhone")
                            // passando os dados do usuario para o qrcode
                            dados.nome = userName
                            dados.telefone = userPhone
                            dados.id = userId
                        } else {
                            Log.d(TAG, "Nome do usuário e celular não encontrados no Firestore")
                        }
                    } else {
                        Log.d(TAG, "Documento do usuário não encontrado no Firestore")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Erro ao recuperar o nome do usuário do Firestore", e)
                }
        }

        // Recuperando dados da locação para passar para o QRCode, junto com os dados do usuário
        if (!valorRecuperado.isNullOrEmpty()) {
            val documentReference = db.collection("Unidades de Locação").document(valorRecuperado)
            documentReference.get()
                .addOnSuccessListener { document ->
                    val pricesArray2 = document.get("prices") as? List<*>
                    val unidade = document.getString("uid")
                    val endereco = document.getString("address")
                    // Verifica qual é o preço associado à opção selecionada
                    val selectedPrice = when (selectedOption) {
                        "30 minutos" -> pricesArray2?.get(0)?.toString()
                        "1 hora" -> pricesArray2?.get(1)?.toString()
                        "2 horas" -> pricesArray2?.get(2)?.toString()
                        "4 horas" -> pricesArray2?.get(3)?.toString()
                        "Ate 18 horas" -> pricesArray2?.get(4)?.toString()
                        else -> ""
                    }

                    // juntando todos os outros dados para passar no QRCode
                    if (selectedOption.isNotEmpty()) {
                        // Verifica se o preço não é nulo ou vazio antes de converter para Double
                        if (!selectedPrice.isNullOrEmpty()) {
                            dados.preco = selectedPrice.toDouble()
                            dados.opcao = selectedOption
                            if (unidade != null) {
                                dados.unidade = unidade
                            }
                            if (endereco != null) {
                                dados.endereco = endereco
                            }
                            Log.d(TAG, "preço: $selectedPrice, Unidade: $unidade, Id: $userId")
                        }
                        // Para transformar o objeto com os dados a serem passados pelo QRcode em string
                        val gson = Gson()
                        val dadosGson = gson.toJson(dados)
                        // Cria um intent para a próxima activity (QRcodeActivity)
                        val intent = Intent(this, QRcodeActivity::class.java)
                        // Serializa o objeto 'dados' para JSON e o passa para a próxima activity
                        intent.putExtra("dados", dadosGson)
                        // Inicia a próxima activity
                        startActivity(intent)
                    } else {
                        // Nenhuma opção selecionada, mostra uma mensagem para o usuário
                        showErrorMessage("Erro: Selecione uma opção antes de continuar.")
                    }
                }
        } else {
            Toast.makeText(this,
                "Erro ao obter documento.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Exibe um diálogo de ERRO customizado com uma mensagem simples e um botão "OK".
     * @param message A mensagem a ser exibida no diálogo de alerta.
     */

    private fun showErrorMessage(message: String) {
        // Inflate o layout personalizado
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog_error, null)

        // Crie o AlertDialog com o layout personalizado
        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        // Configure o botão OK para fechar o diálogo
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }

        // Atualize a mensagem no TextView
        val textViewMessage = view.findViewById<TextView>(R.id.tvMessage)
        textViewMessage.text = message

        // Mostre o diálogo
        alertDialog.show()
    }

    private fun obterLocalizacaoAtual() {

        // Obter a última localização conhecida do usuário
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this,
                "Permissão não concedida.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {

                    latitudeUser = location.latitude
                    longitudeUser = location.longitude

                    Log.d(TAG, "Latitude do usuário: $latitudeUser")
                    Log.d(TAG, "Longitude do usuário: $longitudeUser")

                    carregarDadosBanco()

                } else {
                    Toast.makeText(
                        this,
                        "Não foi possível obter a localização.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Não foi possível obter a localização atual.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun carregarDadosBanco() {
        val sharedPreferences = getSharedPreferences("uid", MODE_PRIVATE)
        val valorRecuperado = sharedPreferences.getString("uid", null)

        Log.d(TAG, "Uid: $valorRecuperado")

        val editLocal = findViewById<TextView>(R.id.local)

        if (!valorRecuperado.isNullOrEmpty()) {
            val documentReference = db.collection("Unidades de Locação").document(valorRecuperado)

            Log.d(TAG, "DocumentReference: $documentReference")

            documentReference.get()
                .addOnSuccessListener { document ->
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")

                    if (document.exists()) {
                        // O documento com o ID recuperado existe, agora você pode acessar seus dados
                        val nome = document.getString("name")

                        // Acessando a latitude e longitude do ponto de locação
                        val latLng = document.getGeoPoint("latLng")

                        if (latLng != null) {
                            val latitude = latLng.latitude
                            val longitude = latLng.longitude

                            Log.d(TAG, "Latitude: $latitude")
                            Log.d(TAG, "Longitude: $longitude")

                            val distanciaLimite = 1 // distância limite em quilômetros

                            val distancia = calcularDistancia(
                                latitudeUser, longitudeUser, latitude, longitude
                            )

                            if (distancia <= distanciaLimite) {
                                // A localização está próxima do ponto de locação

                                editLocal.text = nome

                                val pricesArray = document.get("prices") as? List<*>
                                if (pricesArray != null) {
                                    // Limpar as opções anteriores
                                    clearRadioButtonOptions()
                                    // Adicionar os preços aos RadioButtons
                                    addPricesToRadioButtons(pricesArray)

                                } else {
                                    // O campo 'prices' não é um array ou está vazio
                                    Toast.makeText(
                                        this@LocationActivity,
                                        "Não há preços associados com essa Unidade de Locação.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            } else {
                                // A localização não está próxima do ponto de locação
                                showAlertMessage("Aviso: Você deve estar a, no máximo, 1000 metros do último pin selecionado no mapa para alugar um armário.")
                            }

                        } else {
                            Toast.makeText(
                                this@LocationActivity,
                                "Localização não permitida ou encontrada, tente novamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        // Faça algo para lidar com esse caso, se necessário
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Erro ao pegar o documento", exception)
                }
        } else {
            showAlertMessage("Aviso: Selecione o pin da unidade desejada no mapa antes de tentar alugar um armário.")
            finish()
        }
    }


    private fun clearRadioButtonOptions() {
        // Limpar os textos dos RadioButtons
        binding.btn30min.text = ""
        binding.btn1hour.text = ""
        binding.btn2hours.text = ""
        binding.btn4hours.text = ""
        binding.btnUntil18.text = ""
    }

    private fun addPricesToRadioButtons(prices: List<*>?) {
        // Certifique-se de que há preços suficientes para cada RadioButton
        if (prices != null) {
            if (prices.size >= 5) {
                // Definir os preços nos RadioButtons
                binding.btn30min.text = "30 minutos - R$ ${prices[0]},00"
                binding.btn1hour.text = "1 hora - R$ ${prices[1]},00"
                binding.btn2hours.text = "2 horas - R$ ${prices[2]},00"
                binding.btn4hours.text = "4 horas - R$ ${prices[3]},00"
                binding.btnUntil18.text = "Do momento até 18h - R$ ${prices[4]},00"
            } else {
                // Não há preços suficientes
                Toast.makeText(this, "Não há preços suficientes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Exibe um diálogo de AVISO customizado com uma mensagem simples e um botão "OK".
     * @param message A mensagem a ser exibida no diálogo de alerta.
     */

    private fun showAlertMessage(message: String) {
        // Inflate o layout personalizado
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog_warning, null)

        // Crie o AlertDialog com o layout personalizado
        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        // Configure o botão OK para fechar o diálogo
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        btnOk.setOnClickListener {
            alertDialog.dismiss()
            finish()
        }

        // Atualize a mensagem no TextView
        val textViewMessage = view.findViewById<TextView>(R.id.tvMessage)
        textViewMessage.text = message

        // Mostre o diálogo
        alertDialog.show()
    }

    // cálculo da distância
    private fun calcularDistancia(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0 // raio da Terra em km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(lat1Rad) * cos(lat2Rad)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun checkHour() {
        // Obter a hora atual
        val calendario = Calendar.getInstance()
        val horaAtual: Int = calendario.get(Calendar.HOUR_OF_DAY)  // Obtém a hora atual como um inteiro
        val minutoAtual: Int = calendario.get(Calendar.MINUTE)      // Obtém os minutos atuais

        // Converte os valores para Double antes de realizar a operação de divisão
        val hora: Double = horaAtual.toDouble() + minutoAtual.toDouble() / 60.0

        // Log da hora atual
        Log.d(TAG, "Hora atual: $hora")

        // Verificar se está entre 7 e 8 horas
        if (hora >= 7.0 && hora <= 8.0) {
            binding.btnUntil18.visibility = View.VISIBLE
        } else {
            binding.btnUntil18.visibility = View.GONE
        }
        // Verificar se é mais de 14h
        if (hora > 14.0) {
            binding.btn4hours.visibility = View.GONE
        } else {
            binding.btn4hours.visibility = View.VISIBLE
        }
        // Verificar se é mais de 16h
        if (hora > 16.0) {
            binding.btn2hours.visibility = View.GONE
        } else {
            binding.btn2hours.visibility = View.VISIBLE
        }
        // Verificar se é mais de 17h
        if (hora > 17.0) {
            binding.btn1hour.visibility = View.GONE
        } else {
            binding.btn1hour.visibility = View.VISIBLE
        }
        // Verificar se é mais de 17h30
        if (hora > 17.5) {
            binding.btn30min.visibility = View.GONE
        } else {
            binding.btn30min.visibility = View.VISIBLE
        }
    }
}

// Classe que faz a passagem de dados do cliente para o QRcode
data class DadosCliente(
    var id: String,
    var nome: String,
    var telefone: String,
    var unidade: String,
    var opcao: String,
    var endereco: String,
    var preco: Double
)