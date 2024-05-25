package br.edu.puccampinas.projeto_smart_locker

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.GlobalScope
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class RouteMappingActivity : AppCompatActivity() {
    // declaração do banco e das variaveis que serão usadas para representar as localizações/unidades
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var userLatLng: LatLng
    private lateinit var places: List<Place>
    private lateinit var tv_address: TextView
    private lateinit var tvInfo: TextView
    private lateinit var tvName: TextView
    private lateinit var btnAction: Button
    private lateinit var placeName: String
    private var placeLatitude: Double = 0.0
    private var placeLongitude: Double = 0.0
    private lateinit var placeAddress: String
    private lateinit var placeLatLng: LatLng
    private lateinit var placeUid: String
    private lateinit var botaoVoltarMapa: ImageView

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_mapping)

        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicialize as propriedades com os valores do Intent
        placeName = intent.getStringExtra("placeName") ?: ""
        placeAddress = intent.getStringExtra("placeAddress") ?: ""

        tvInfo = findViewById(R.id.textInfo)
        btnAction = findViewById(R.id.button)
        tvName = findViewById(R.id.tv_name)
        tv_address = findViewById(R.id.tv_address)
        botaoVoltarMapa = findViewById(R.id.btnVoltar)

        // declarando o nome e endereço da unidade para printar respectivamente no layout
        tvName.text = placeName
        tv_address.text = placeAddress

        updateUIForUserState()

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            googleMap = map
            setupMap()
        }

        botaoVoltarMapa.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        btnAction.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                // Usuário está logado
                val intent = Intent(this, ClientMainScreenActivity::class.java)

                // Adiciona as flags para limpar a pilha de atividades e iniciar uma nova tarefa
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                // Inicia a MainActivity
                startActivity(intent)

                // Finaliza a RotaActivity
                finish()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Usuário não está logado
                    val intent = Intent(this, OpeningActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
            }
        }
    }
    private fun updateUIForUserState() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Usuário está logado
            btnAction.text = " Voltar ao menu"
            tvInfo.text = "Para alugar um armário no local selecionado, volte ao menu inicial e clique em 'Alugar Armário'."
        } else {
            // Usuário não está logado
            btnAction.text = " Voltar à página inicial"
            tvInfo.text = "Para alugar um armário no local selecionado, você precisa estar conectado."
        }
    }
    private fun setupMap() {
        // define um adaptador para as janelas de informações dos marcadores
        googleMap.setInfoWindowAdapter(MarkerInfoAdapter(this))

        // verifica se a permissão de localização está concedida
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // obtém a última localização conhecida do dispositivo
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    // define a localização do usuário
                    // obtém as coordenadas do local de destino do intent
                    userLatLng = LatLng(location.latitude, location.longitude)
                    placeLatitude = intent.getDoubleExtra("placeLatitude", 0.0)
                    placeLongitude = intent.getDoubleExtra("placeLongitude", 0.0)
                    placeLatLng = LatLng(placeLatitude, placeLongitude)
                    // traça a rota no mapa
                    traceRoute(userLatLng, placeLatLng)
                    // move a câmera para a posição do usuário
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            userLatLng,
                            15f
                        )
                    )
                    // adiciona um marcador na posição do usuário
                    googleMap.addMarker(
                        MarkerOptions().position(userLatLng).title("Sua Localização")
                    )
                    loadPlacesFromFirestore()
                }
            }
        } else {
            // solicita permissão de acesso à localização
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun loadPlacesFromFirestore() {
        // obtém a coleção de lugares do Firestore
        val placesCollection = firestore.collection("Unidades de Locação")

        // obtém os documentos da coleção
        placesCollection.get()
            .addOnSuccessListener { documents ->
                // mapeia os documentos para objetos Place
                places = documents.mapNotNull { document ->
                    val name = document.getString("name") ?: ""
                    val latitude = document.getGeoPoint("latLng")?.latitude ?: 0.0
                    val longitude = document.getGeoPoint("latLng")?.longitude ?: 0.0
                    val latLng = GeoPoint(latitude, longitude)
                    val address = document.getString("address") ?: ""
                    val reference = document.getString("reference") ?: ""
                    val prices = document.get("prices") as? List<Double> ?: emptyList()
                    val uid = document.getString("uid") ?: ""
                    Place(name, latLng, address, reference, prices, uid)
                }
                // adiciona marcadores para os lugares no mapa
                addMarkers()
            }
            .addOnFailureListener { exception ->
                // exibe uma mensagem de erro se falhar ao obter os lugares do Firestore
                Toast.makeText(
                    this@RouteMappingActivity,
                    "Erro ao carregar os lugares do banco de dados.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addMarkers() {
        // itera sobre a lista de lugares
        places.forEach { place ->
            val marker = place.latLng?.let { geoPoint ->
                // adiciona um marcador para cada lugar no mapa
                googleMap.addMarker(
                    MarkerOptions()
                        .title(place.name)
                        .snippet(place.address)
                        .position(LatLng(geoPoint.latitude, geoPoint.longitude))
                        .icon(
                            BitmapHelper.vectorToBitmap(
                                this, R.drawable.icon_custom_pin,
                                //ContextCompat.getColor(this, R.color.orange)
                            )
                        )
                )
            }
            marker?.tag = place
        }
    }

    private fun traceRoute(origin: LatLng, destination: LatLng) {
        // configuração do GeoApiContext com a chave de API
        val geoApiContext = GeoApiContext.Builder()
            .apiKey("AIzaSyBCzkVPB40LH6dVBphGEgLp7-Ydu5JcGMI")
            .build()
        // executa a solicitação assíncrona para traçar a rota
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result: DirectionsResult = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(
                        com.google.maps.model.LatLng(
                            destination.latitude,
                            destination.longitude
                        )
                    )
                    .await()
                // exibe o resultado da solicitação de rota no log
                Log.d("RotaActivity", "Resultado da rota recebido com sucesso: $result")
                // atualiza a UI principal para desenhar a rota no mapa
                runOnUiThread {
                    drawRoute(result)
                }
            } catch (e: Exception) {
                // manipula erros ao traçar a rota
                Log.e("RotaActivity", "Erro ao traçar rota: ${e.message}")
            }
        }
    }
    // configurações do desenho do traçado da rota
    private fun drawRoute(result: DirectionsResult) {
        Log.d("RotaActivity", "Desenhando rota no mapa")

        // Defina a cor da linha (por exemplo, azul) e a espessura desejada (por exemplo, 10f)
        val color = Color.BLACK
        val width = 7f

        val options = PolylineOptions()
        options.color(color)
        options.width(width)

        for (route in result.routes) {
            for (leg in route.legs) {
                for (step in leg.steps) {
                    val points = step.polyline.decodePath()
                    for (point in points) {
                        options.add(LatLng(point.lat, point.lng))
                    }
                }
            }
        }

        googleMap.addPolyline(options)
    }

}

