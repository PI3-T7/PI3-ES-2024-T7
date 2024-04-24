package br.edu.puccampinas.projeto_smart_locker

import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
import android.content.Intent
import android.graphics.Color
import android.util.Log
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

class RotaActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var userLatLng: LatLng
    private lateinit var places: List<Place>
    private lateinit var tvDestino: TextView
    private lateinit var placeName: String
    private var placeLatitude: Double = 0.0
    private var placeLongitude: Double = 0.0
    private lateinit var placeAddress: String
    private lateinit var placeLatLng: LatLng
    private lateinit var botaoVoltarMapa: ImageView

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rota)

        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicialize as propriedades com os valores do Intent
        placeName = intent.getStringExtra("placeName") ?: ""
        placeAddress = intent.getStringExtra("placeAddress") ?: ""

        tvDestino = findViewById(R.id.textLocalLocacao)
        botaoVoltarMapa = findViewById(R.id.voltarMapa)

        // Concatenando o nome do local e o endereço
        val destinationText = "$placeName - $placeAddress"
        tvDestino.text = destinationText

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
    }

    private fun setupMap() {
        googleMap.setInfoWindowAdapter(MarkerInfoAdapter(this))

        googleMap.setOnMapClickListener {
            hideInfoView()
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    userLatLng = LatLng(location.latitude, location.longitude)
                    placeLatitude = intent.getDoubleExtra("placeLatitude", 0.0)
                    placeLongitude = intent.getDoubleExtra("placeLongitude", 0.0)
                    placeLatLng = LatLng(placeLatitude, placeLongitude)
                    traceRoute(userLatLng, placeLatLng)
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            userLatLng,
                            15f
                        )
                    )
                    googleMap.addMarker(
                        MarkerOptions().position(userLatLng).title("Sua Localização")
                    )
                    loadPlacesFromFirestore()
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun loadPlacesFromFirestore() {
        val placesCollection = firestore.collection("Unidades de Locação")

        placesCollection.get()
            .addOnSuccessListener { documents ->
                places = documents.mapNotNull { document ->
                    val name = document.getString("name") ?: ""
                    val latitude = document.getGeoPoint("latLng")?.latitude ?: 0.0
                    val longitude = document.getGeoPoint("latLng")?.longitude ?: 0.0
                    val latLng = GeoPoint(latitude, longitude)
                    val address = document.getString("address") ?: ""
                    val reference = document.getString("reference") ?: ""
                    val prices = document.get("prices") as? List<Double> ?: emptyList()
                    Place(name, latLng, address, reference, prices)
                }

                addMarkers()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@RotaActivity,
                    "Erro ao carregar os lugares do banco de dados.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addMarkers() {
        places.forEach { place ->
            val marker = place.latLng?.let { geoPoint ->
                googleMap.addMarker(
                    MarkerOptions()
                        .title(place.name)
                        .snippet(place.address)
                        .position(LatLng(geoPoint.latitude, geoPoint.longitude))
                        .icon(
                            BitmapHelper.vectorToBitmap(
                                this, R.drawable.icon_pin,
                                //ContextCompat.getColor(this, R.color.orange)
                            )
                        )
                )
            }
            marker?.tag = place
        }
    }

    private fun hideInfoView() {
        val viewInfo = findViewById<View>(R.id.container_info_map)
        val newHeight = 1
        val anim = ValueAnimator.ofInt(viewInfo.height, newHeight)
        anim.addUpdateListener { valueAnimator ->
            val newVal = valueAnimator.animatedValue as Int
            val layoutParams = viewInfo.layoutParams
            layoutParams.height = newVal
            viewInfo.layoutParams = layoutParams
        }
        anim.duration = 300
        anim.start()

        findViewById<TextView>(R.id.tv_name)?.text = ""
        findViewById<TextView>(R.id.tv_address)?.text = ""
        findViewById<TextView>(R.id.tv_reference)?.text = ""
    }

    private fun traceRoute(origin: LatLng, destination: LatLng) {
        val geoApiContext = GeoApiContext.Builder()
            .apiKey("AIzaSyBCzkVPB40LH6dVBphGEgLp7-Ydu5JcGMI")
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result: DirectionsResult = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .await()

                Log.d("RotaActivity", "Resultado da rota recebido com sucesso: $result")

                runOnUiThread {
                    drawRoute(result)
                }
            } catch (e: Exception) {
                Log.e("RotaActivity", "Erro ao traçar rota: ${e.message}")
            }
        }
    }

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

