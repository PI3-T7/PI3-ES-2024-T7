package br.edu.puccampinas.projeto_smart_locker

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.util.*
import kotlin.math.*

class LocationActivity : AppCompatActivity() {

    // Variável que acessa os serviços de localização da API
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var latitudeUser: Double = 0.0
    private var longitudeUser: Double = 0.0

    companion object {
        // Código de solicitação de permissão
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    private lateinit var btn30min: RadioButton
    private lateinit var btn1hour: RadioButton
    private lateinit var btn2hours: RadioButton
    private lateinit var btn4hours: RadioButton
    private lateinit var btnUntil18: RadioButton
    private lateinit var btnConfirmLocation: Button
    private lateinit var botaoVoltar1: ImageView
    private lateinit var buttonHome1: ImageView

    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        btn30min = findViewById(R.id.btn30min)
        btn1hour = findViewById(R.id.btn1hour)
        btn2hours = findViewById(R.id.btn2hours)
        btn4hours = findViewById(R.id.btn4hours)
        btnUntil18 = findViewById(R.id.btnUntil18)
        btnConfirmLocation = findViewById(R.id.bt_confirm_location)
        botaoVoltar1 = findViewById(R.id.buttonVoltar1)
        buttonHome1 = findViewById(R.id.buttonHome1)

        verificarCartaoCadastrado()

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

        botaoVoltar1.setOnClickListener {
            val intent = Intent(this, ClientMainScreenActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonHome1.setOnClickListener {
            val intent = Intent(this, ClientMainScreenActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Esta parte do codigo muda a cor do RadioButton ao ser selecionado, e mantém o arredondamento
        btn30min.setOnClickListener {
            changeColorRadio()
            findViewById<RadioButton>(R.id.btn30min).apply {
                isChecked = true
                setBackgroundColor(
                    ContextCompat.getColor(
                        this@LocationActivity,
                        R.color.color_checked
                    )
                )
                background = ContextCompat.getDrawable(context, R.drawable.container_check)
            }
        }

        btn1hour.setOnClickListener {
            changeColorRadio()
            findViewById<RadioButton>(R.id.btn1hour).apply {
                isChecked = true
                setBackgroundColor(
                    ContextCompat.getColor(
                        this@LocationActivity,
                        R.color.color_checked
                    )
                )
                background = ContextCompat.getDrawable(context, R.drawable.container_check)
            }
        }

        btn2hours.setOnClickListener {
            changeColorRadio()
            findViewById<RadioButton>(R.id.btn2hours).apply {
                isChecked = true
                setBackgroundColor(
                    ContextCompat.getColor(
                        this@LocationActivity,
                        R.color.color_checked
                    )
                )
                background = ContextCompat.getDrawable(context, R.drawable.container_check)
            }
        }

        btn4hours.setOnClickListener {
            changeColorRadio()
            findViewById<RadioButton>(R.id.btn4hours).apply {
                isChecked = true
                setBackgroundColor(
                    ContextCompat.getColor(
                        this@LocationActivity,
                        R.color.color_checked
                    )
                )
                background = ContextCompat.getDrawable(context, R.drawable.container_check)
            }
        }

        btnUntil18.setOnClickListener {
            changeColorRadio()
            findViewById<RadioButton>(R.id.btnUntil18).apply {
                isChecked = true
                setBackgroundColor(
                    ContextCompat.getColor(
                        this@LocationActivity,
                        R.color.color_checked
                    )
                )
                background = ContextCompat.getDrawable(context, R.drawable.container_check)
            }
        }

        // Evento do botão que confirma a locação e chama a Activity para gerar o QRcode
        btnConfirmLocation.setOnClickListener {
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
        var dados = DadosCliente("", "", "", 0.0)
        // Verifica qual RadioButton está selecionado e atribui a opção correspondente ao objeto 'dados'
        val selectedOption = when {
            btn30min.isChecked -> "30 minutos"
            btn1hour.isChecked -> "1 hora"
            btn2hours.isChecked -> "2 horas"
            btn4hours.isChecked -> "4 horas"
            btnUntil18.isChecked -> "Até às 18 horas"
            else -> ""
        }

        Log.d(TAG, "Selected Option: $selectedOption")

        val sharedPreferences = getSharedPreferences("uid", MODE_PRIVATE)
        val valorRecuperado = sharedPreferences.getString("uid", null)
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (!valorRecuperado.isNullOrEmpty()) {
            val documentReference = db.collection("Unidades de Locação").document(valorRecuperado)
            documentReference.get()
                .addOnSuccessListener { document ->
                    val pricesArray2 = document.get("prices") as? List<*>
                    val unidade = document.getString("uid")
                    // Verifica qual é o preço associado à opção selecionada
                    val selectedPrice = when (selectedOption) {
                        "30 minutos" -> pricesArray2?.get(0)?.toString()
                        "1 hora" -> pricesArray2?.get(1)?.toString()
                        "2 horas" -> pricesArray2?.get(2)?.toString()
                        "4 horas" -> pricesArray2?.get(3)?.toString()
                        "Até às 18 horas" -> pricesArray2?.get(4)?.toString()
                        else -> ""
                    }

                    if (selectedOption.isNotEmpty()) {
                        // Verifica se o preço não é nulo ou vazio antes de converter para Double
                        if (!selectedPrice.isNullOrEmpty()) {
                            dados.preco = selectedPrice.toDouble()
                            dados.opcao = selectedOption
                            if (unidade != null) {
                                dados.unidade = unidade
                            }
                            if (userId != null) {
                                dados.nome = userId
                            }
                            Log.d(TAG, "preço: $selectedPrice, Unidade: $unidade, Nome: $userId")
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
                        Toast.makeText(this,
                            "Por favor, selecione uma opção antes de confirmar a locação.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(this,
                "Erro ao obter documento.",
                Toast.LENGTH_SHORT
            ).show()
        }
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
                                val toast = Toast.makeText(
                                    applicationContext,
                                    "\"Você não está próximo(a) de nenhuma Unidade SmartLocker. Esteja a pelo menos 100 metros de uma e tente novamente.\"",
                                    Toast.LENGTH_LONG
                                )
                                val view = layoutInflater.inflate(R.layout.custom_toast_layout, null)
                                val text = view.findViewById<TextView>(R.id.text)
                                text.text =
                                    "Você deve estar a pelo menos 1000 metros do último pin selecionado no mapa."
                                toast.view = view
                                toast.show()

                                finish()
                            }

                        } else {
                            Toast.makeText(
                                this@LocationActivity,
                                "Localização não permitida ou encontrada, tente novamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                    } else {
                        // Faça algo para lidar com esse caso, se necessário
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Erro ao pegar o documento", exception)
                }
        } else {
            Toast.makeText(
                this@LocationActivity,
                "Por favor, selecione um pin no mapa antes de tentar alugar um armário.",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }


    private fun clearRadioButtonOptions() {
        // Limpar os textos dos RadioButtons
        btn30min.text = ""
        btn1hour.text = ""
        btn2hours.text = ""
        btn4hours.text = ""
        btnUntil18.text = ""
    }

    private fun addPricesToRadioButtons(prices: List<*>?) {
        // Certifique-se de que há preços suficientes para cada RadioButton
        if (prices != null) {
            if (prices.size >= 5) {
                // Definir os preços nos RadioButtons
                btn30min.text = "30 minutos                                                ${prices[0]},00"
                btn1hour.text = "1 hora                                                         ${prices[1]},00"
                btn2hours.text = "2 horas                                                       ${prices[2]},00"
                btn4hours.text = "4 horas                                                       ${prices[3]},00"
                btnUntil18.text = "Do momento até 18h                               ${prices[4]},00"
            } else {
                // Não há preços suficientes
                Toast.makeText(this, "Não há preços suficientes", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        val horaAtualDouble: Double = horaAtual.toDouble() + minutoAtual.toDouble() / 60.0

        // Log da hora atual
        Log.d(TAG, "Hora atual: $horaAtualDouble")

        // Verificar se está entre 7 e 8 horas
        if (horaAtualDouble >= 7.0 && horaAtualDouble <= 8.0) {
            Log.d(TAG, "Hora está entre 7 e 8 horas.")
            btnUntil18.visibility = View.VISIBLE
        } else {
            Log.d(TAG, "Hora não está entre 7 e 8 horas.")
            btnUntil18.visibility = View.GONE
        }
    }

    // Essa função muda a cor do radiobutton para branco de novo quando outro for selecionado
    // e mantém o formato arredondado.
    private fun changeColorRadio() {
        val radioButtonIds = listOf(
            R.id.btn30min,
            R.id.btn1hour,
            R.id.btn2hours,
            R.id.btn4hours,
            R.id.btnUntil18
        )

        for (radioButtonId in radioButtonIds) {
            val radioButton = findViewById<RadioButton>(radioButtonId)
            if (!radioButton.isChecked) {
                radioButton.apply {
                    setBackgroundColor(ContextCompat.getColor(this@LocationActivity, android.R.color.white))
                    background = ContextCompat.getDrawable(context, R.drawable.container_check2)
                }
            }
        }
    }
    private fun verificarCartaoCadastrado() {
        // Pegando o ID do usuário logado
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        if (userId != null) {
            db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Verifica se o campo "cartoes" existe no documento
                        if (document.contains("cartoes")) {
                            // Obtém o array de cartões do documento
                            val cartoes = document.get("cartoes") as? ArrayList<HashMap<String, String>>

                            if (cartoes.isNullOrEmpty()) {
                                // Se não houver cartões
                                Toast.makeText(this, "Nenhum cartão cadastrado, cadastre um antes de realizar uma locação.", Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Erro ao acessar documento: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }

}

// Classe apenas para testar a passagem de dados do cliente para o QRcode
data class DadosCliente(
    var nome: String,
    var unidade: String,
    var opcao: String,
    var preco: Double
)