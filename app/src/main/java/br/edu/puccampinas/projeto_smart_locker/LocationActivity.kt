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
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

        checkHour()

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

        // Esta parte do codigo muda a cor do RadioButton ao ser selecionado, e mantém o arredondamento
        btn30min.setOnClickListener {
            changeColorRadio()
            findViewById<RadioButton>(R.id.btn30min).apply {
                isChecked = true
                setBackgroundColor(
                    ContextCompat.getColor(
                        this@LocationActivity,
                        R.color.cor_checked
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
                        R.color.cor_checked
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
                        R.color.cor_checked
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
                        R.color.cor_checked
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
                        R.color.cor_checked
                    )
                )
                background = ContextCompat.getDrawable(context, R.drawable.container_check)
            }
        }

        // Para transformar o objeto com os dados a serem passados pelo QRcode em json
        val gson = Gson()

        val dados = DadosCliente("Isabella","Unidade 3", "2 horas", 55.0)
        val dadosGson = gson.toJson(dados)

        // Evento do botão que confirma a locação e chama a Activity para gerar o QRcode
        btnConfirmLocation.setOnClickListener {
            val intent = Intent(this, QRcodeActivity::class.java)
            intent.putExtra("dados", dadosGson)
            startActivity(intent)
        }

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

        val editLocal = findViewById<TextView>(R.id.local)

        val documentReference =
            db.collection("Unidades de Locação").document("sxtHaqQFSv89iceO0kD0")

        Log.d(TAG, "DocumentReference: $documentReference")

        documentReference.get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")

                if (document.exists()) {
                    // O documento com o ID "1" existe, agora você pode acessar seus dados
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
                                "Você deve estar a pelo menos 100 metros do ponto de locação escolhido para alugar um armário!"
                            toast.view = view
                            toast.show()

                            finish()
                        }

                    } else {
                        Toast.makeText(
                            this@LocationActivity,
                            "Geopoint nulo.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    // O documento com o ID "1" não existe
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting document", exception)
                // Lidar com falhas na recuperação aqui
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
                btn30min.text =
                    "30 minutos                                                                ${
                        prices?.get(0)
                    },00"
                btn1hour.text =
                    "1 hora                                                                          ${
                        prices?.get(1)
                    },00"
                btn2hours.text =
                    "2 horas                                                                      ${
                        prices?.get(2)
                    },00"
                btn4hours.text =
                    "4 horas                                                                      ${
                        prices?.get(3)
                    },00"
                btnUntil18.text =
                    "Do momento até 18h                                          ${prices?.get(4)},00"
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
        val horaAtual = calendario.get(Calendar.HOUR_OF_DAY)

        // Verificar se está entre 7 e 8 horas
        if (horaAtual in 7..8) {
            btnUntil18.visibility = View.VISIBLE
        } else {
            btnUntil18.visibility = View.GONE
        }
    }

    private fun changeColorRadio() {
        findViewById<RadioButton>(R.id.btn30min).apply {
            isChecked = false
            setBackgroundColor(ContextCompat.getColor(this@LocationActivity, android.R.color.white))
            background = ContextCompat.getDrawable(context, R.drawable.container_check2)
        }
        findViewById<RadioButton>(R.id.btn1hour).apply {
            isChecked = false
            setBackgroundColor(ContextCompat.getColor(this@LocationActivity, android.R.color.white))
            background = ContextCompat.getDrawable(context, R.drawable.container_check2)
        }
        findViewById<RadioButton>(R.id.btn2hours).apply {
            isChecked = false
            setBackgroundColor(ContextCompat.getColor(this@LocationActivity, android.R.color.white))
            background = ContextCompat.getDrawable(context, R.drawable.container_check2)
        }
        findViewById<RadioButton>(R.id.btn4hours).apply {
            isChecked = false
            setBackgroundColor(ContextCompat.getColor(this@LocationActivity, android.R.color.white))
            background = ContextCompat.getDrawable(context, R.drawable.container_check2)
        }
        findViewById<RadioButton>(R.id.btnUntil18).apply {
            isChecked = false
            setBackgroundColor(ContextCompat.getColor(this@LocationActivity, android.R.color.white))
            background = ContextCompat.getDrawable(context, R.drawable.container_check2)
        }
    }
}

// Classe apenas para testar a passagem de dados do cliente para o QRcode
data class DadosCliente(
    val nome: String,
    val unidade: String,
    val opcao: String,
    val preco: Double
)