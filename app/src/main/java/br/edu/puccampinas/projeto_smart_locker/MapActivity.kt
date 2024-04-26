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
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class MapActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private var selectedPlace: Place? = null
    private lateinit var botaoAberturaVoltar: ImageView

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        hideInfoView()

        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        botaoAberturaVoltar = findViewById(R.id.voltarOpening)
        val botaoRota = findViewById<Button>(R.id.btnRotas)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            googleMap = map
            loadPlacesFromFirestore()
            setupMap()
        }

        botaoRota.setOnClickListener {
            if (selectedPlace != null) {
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
        }
        val vindoDaOpeningActivity = intent.getBooleanExtra("vindo_da_opening_activity", true)
        val vindoDaTeladoUsuario = intent.getBooleanExtra("vindo_da_tela_usuário", false)

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

    private fun setupMap() {
        googleMap.setInfoWindowAdapter(MarkerInfoAdapter(this))

        googleMap.setOnMapClickListener {
            hideInfoView()
        }

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

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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
                val places = mutableListOf<Place>()

                for (document in documents) {
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
                    Log.d("Firestore", "Place Name: $name, LatLng: $latLng, Address: $address, Reference: $reference, Prices: $prices, Uid: $uid")
                }

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

    private fun addMarkers(places: List<Place>) {
        places.forEach { place ->
            val marker = place.latLng?.let { geoPoint ->
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

    private fun showPlaceInfo(place: Place) {
        val viewInfo = findViewById<View>(R.id.container_info_map)
        findViewById<TextView>(R.id.tv_name)?.text = place.name
        findViewById<TextView>(R.id.tv_address)?.text = place.address
        findViewById<TextView>(R.id.tv_reference)?.text = place.reference

        val newHeight = 650
        val anim = ValueAnimator.ofInt(viewInfo.height, newHeight)
        anim.addUpdateListener { valueAnimator ->
            val newVal = valueAnimator.animatedValue as Int
            val layoutParams = viewInfo.layoutParams
            layoutParams.height = newVal
            viewInfo.layoutParams = layoutParams
        }
        anim.duration = 300
        anim.start()
    }
}

// Data class para representar um lugar
data class Place(
    val name: String,
    val latLng: GeoPoint?,
    val address: String,
    val reference: String,
    val prices: List<Double>,
    val uid: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Place

        return prices == other.prices
    }

    override fun hashCode(): Int {
        return prices.hashCode()
    }
}