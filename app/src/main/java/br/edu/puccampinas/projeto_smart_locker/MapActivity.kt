package br.edu.puccampinas.projeto_smart_locker

import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
/**
 * Activity responsável pela criação/inserção da atividade do mapa.
 * @authors: Lais e Isabella.
 */
class MapActivity : AppCompatActivity() {
    // Declaração de variáveis e inicialização
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private var selectedPlace: Place? = null
    private lateinit var botaoAberturaVoltar: ImageView

    // Inicialização de uma instância de NetworkChecker para verificar a conectividade de rede.
    private val networkChecker by lazy {
        NetworkChecker(
            ContextCompat.getSystemService(this, ConnectivityManager::class.java)
                ?: throw IllegalStateException("ConnectivityManager not available")
        )
    }

    companion object {
        // Constante para solicitar permissão de localização
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    // Suprime a chamada ao super.onBackPressed() para desabilitar o botão de voltar padrão
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
    }

    /**
     * Marca que o método só deve ser chamado em versões O e superiores do Android
     * @authors: Lais.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Inicializa os componentes e configura o mapa
        hideInfoView()
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        botaoAberturaVoltar = findViewById(R.id.voltarOpening)
        val botaoRota = findViewById<Button>(R.id.btnRotas)

        // Configuração do fragmento do mapa
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        // Carrega os lugares do Firestore e configura o mapa
        mapFragment.getMapAsync { map ->
            googleMap = map
            loadPlacesFromFirestore()
            setupMap()
        }

        // Lida com o clique no botão para mapeamento de rotas
        botaoRota.setOnClickListener {
            // Verifica a conectividade de rede antes de proceder
            if (networkChecker.hasInternet()) {
                if (selectedPlace != null) {
                    // Inicia a atividade de mapeamento de rotas com os dados do lugar selecionado
                    val intent = Intent(this, RouteMappingActivity::class.java)
                    intent.putExtra("placeName", selectedPlace!!.name)
                    intent.putExtra("placeLatitude", selectedPlace!!.latLng?.latitude)
                    intent.putExtra("placeLongitude", selectedPlace!!.latLng?.longitude)
                    intent.putExtra("placeAddress", selectedPlace!!.address)
                    intent.putExtra("placeUid", selectedPlace!!.uid)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Selecione um lugar primeiro", Toast.LENGTH_SHORT).show()
                }
            } else {
                startActivity(Intent(this, NetworkErrorActivity::class.java))
            }
        }

        // Verifica de qual atividade anterior a navegação foi iniciada
        val vindoDaOpeningActivity = intent.getBooleanExtra("vindo_da_opening_activity", true)
        val vindoDaTeladoUsuario = intent.getBooleanExtra("vindo_da_tela_usuário", false)

        // Configura o botão de voltar para direcionar à atividade apropriada
        botaoAberturaVoltar.setOnClickListener {
            if (vindoDaTeladoUsuario) {
                val intent = Intent(this, ClientMainScreenActivity::class.java)
                startActivity(intent)
                finish() // Termina a atividade atual para evitar que ela fique na pilha de atividades
            } else if (vindoDaOpeningActivity) {
                val intent = Intent(this, OpeningActivity::class.java)
                startActivity(intent)
                finish() // Termina a atividade atual para evitar que ela fique na pilha de atividades
            }
        }
    }

    /**
     * Configuração do mapa
     * @authors: Lais.
     */
    private fun setupMap() {
        googleMap.setInfoWindowAdapter(MarkerInfoAdapter(this))

        // Esconde a visualização de informações quando o mapa é clicado
        googleMap.setOnMapClickListener {
            hideInfoView()
        }
        // Mostra as informações do lugar quando um marcador é clicado
        googleMap.setOnMarkerClickListener { marker ->
            val place = marker.tag as? Place
            if (place != null) {
                selectedPlace = place
                val uid = selectedPlace!!.uid
                val sharedPreferences = getSharedPreferences("uid", MODE_PRIVATE)

                // Edita o SharedPreferences para salvar o UID
                val editor = sharedPreferences.edit()
                editor.putString("uid", uid)
                editor.apply()
                showPlaceInfo(place)
                true
            } else {
                false
            }
        }

        // Verifica a permissão de acesso à localização fina
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Obtém a última localização conhecida do dispositivo
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            userLatLng,
                            15f
                        )
                    )
                    googleMap.addMarker(
                        MarkerOptions().position(userLatLng).title("Sua Localização")
                    )
                }
            }
        } else {
            // Solicita a permissão de acesso à localização fina
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Função que carrega unidades que estão no Firestore
     * @authors: Lais.
     */
    private fun loadPlacesFromFirestore() {
        val placesCollection = firestore.collection("Unidades de Locação")

        placesCollection.get()
            .addOnSuccessListener { documents ->
                val places = mutableListOf<Place>()

                for (document in documents) {
                    // Obtém os dados do documento e cria instâncias de Place
                    val name = document.getString("name") ?: ""
                    val latitude = document.getGeoPoint("latLng")?.latitude ?: 0.0
                    val longitude = document.getGeoPoint("latLng")?.longitude ?: 0.0
                    val latLng = GeoPoint(latitude, longitude)

                    Log.d(ContentValues.TAG, "Latitude: $latitude")
                    Log.d(ContentValues.TAG, "Longitude: $longitude")

                    val address = document.getString("address") ?: ""
                    val reference = document.getString("reference") ?: ""
                    val prices = document.get("prices") as? List<Double> ?: emptyList()
                    val uid = document.getString("uid") ?: ""

                    val place = Place(name, latLng, address, reference, prices, uid)
                    places.add(place)

                    // Adicionando log para depuração
                    Log.d(
                        "Firestore",
                        "Place Name: $name, LatLng: $latLng, Address: $address, Reference: $reference, Prices: $prices, Uid: $uid"
                    )
                }

                // Adiciona marcadores ao mapa
                addMarkers(places)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@MapActivity,
                    "Não foi possivel pegar as informações do banco.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Adiciona marcadores no mapa
     * @authors: Lais.
     */
    private fun addMarkers(places: List<Place>) {
        places.forEach { place ->
            val marker = place.latLng?.let { geoPoint ->
                // Adiciona um marcador no mapa com as informações da unidade, caso não nulas.
                googleMap.addMarker(
                    MarkerOptions()
                        // Define o título do marcador com o nome da unidade.
                        .title(place.name)
                        // Define a descrição do marcador com o endereço da unidade.
                        .snippet(place.address)
                        // Define a posição do marcador com a latitude e longitude da unidade.
                        .position(LatLng(geoPoint.latitude, geoPoint.longitude))
                        // Define o ícone personalizado para o marcador
                        .icon(
                            BitmapHelper.vectorToBitmap(
                                this, R.drawable.icon_custom_pin,
                                // ContextCompat.getColor(this, R.color.orange)
                            )
                        )
                )
            }
            // Se o marcador foi criado (não nulo), define a propriedade 'tag' do marcador com o lugar
            marker?.tag = place
        }
    }

    /**
     * Esconde a visualização de informações.
     * @authors: Isabella.
     */
    private fun hideInfoView() {
        // Obtém a referência para a view que contém as informações da unidade
        val viewInfo = findViewById<View>(R.id.container_info_map)
        // Define a nova altura da view
        val newHeight = 1
        // Cria uma animação para mudar a altura da view
        val anim = ValueAnimator.ofInt(viewInfo.height, newHeight)
        // Adiciona um listener para atualizar a altura da view durante a animação
        anim.addUpdateListener { valueAnimator ->
            val newVal = valueAnimator.animatedValue as Int
            val layoutParams = viewInfo.layoutParams
            layoutParams.height = newVal
            viewInfo.layoutParams = layoutParams
        }
        // Define a duração da animação com 300 ms
        anim.duration = 300

        anim.start()
        // Limpa os campos de texto das informações
        findViewById<TextView>(R.id.tv_name)?.text = ""
        findViewById<TextView>(R.id.tv_address)?.text = ""
        findViewById<TextView>(R.id.tv_reference)?.text = ""
    }

    /**
     * Mostra as informações do lugar.
     * @authors: Isabella.
     */
    private fun showPlaceInfo(place: Place) {
        // Obtém a referência para a view que contém as informações da unidade
        val viewInfo = findViewById<View>(R.id.container_info_map)
        // Define os campos de texto com as informações do lugar
        findViewById<TextView>(R.id.tv_name)?.text = place.name
        findViewById<TextView>(R.id.tv_address)?.text = place.address
        findViewById<TextView>(R.id.tv_reference)?.text = place.reference

        // Define a nova altura da view como 800
        val newHeight = 800

        // Cria uma animação para mudar a altura da view
        val anim = ValueAnimator.ofInt(viewInfo.height, newHeight)

        // Adiciona um listener para atualizar a altura da view durante a animação
        anim.addUpdateListener { valueAnimator ->
            val newVal = valueAnimator.animatedValue as Int
            val layoutParams = viewInfo.layoutParams
            layoutParams.height = newVal
            viewInfo.layoutParams = layoutParams
        }
        // Define a duração da animação como 300 ms
        anim.duration = 300
        anim.start()
    }
}
/**
 * Data class para representar um lugar
 * @authors: Lais.
 */
data class Place(
    val name: String,
    val latLng: GeoPoint?,
    val address: String,
    val reference: String,
    val prices: List<Double>,
    val uid: String
) {
    // Sobrescrita do método equals para comparar instâncias de Place
    override fun equals(other: Any?): Boolean {
        // Verifica se é a mesma instância
        if (this === other) return true
        // Verifica se são de classes diferentes
        if (javaClass != other?.javaClass) return false

        other as Place

        return prices == other.prices
    }
    // Sobrescrita do método hashCode para retornar o hash baseado na lista de preços
    override fun hashCode(): Int {
        return prices.hashCode()
    }
}
