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
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class RotaActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var userLatLng: LatLng
    private lateinit var places: List<Place>

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rota)

        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            googleMap = map
            setupMap()
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

        if (places.isNotEmpty()) {
            val firstPlace = places[0]
            traceRoute(userLatLng, firstPlace.latLng)
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

    private fun traceRoute(origin: LatLng, destination: GeoPoint?) {
        val url = "https://www.google.com/maps/dir/?api=1&origin=${origin.latitude},${origin.longitude}&destination=${destination?.latitude},${destination?.longitude}&travelmode=driving"
        Log.d("Route URL", url)
        // Aqui você pode abrir uma WebView ou uma intent para abrir o Google Maps com a rota
    }
}
