package br.edu.puccampinas.projeto_smart_locker

import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat

class MapActivity : AppCompatActivity() {

    private val places = arrayListOf(
        Place(
            "SmartLocker unidade Puc Campinas",
            LatLng(-22.8352868, -47.0603403),
            "Ac. Publico, 286 - Parque dos Jacarandás, Campinas - SP, 13086-061",
            "Próximo ao portão 2"
        ),
        Place(
            "Smart Locker unidade Shopping Dom Pedro",
            LatLng(-22.8475663, -47.0631045),
            "Av. Guilherme Campos, 500 - Jardim Santa Genebra, Campinas - SP, 13080-000",
            "Próximo à Entrada das Águas"
        )
    )

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val viewInfo = findViewById<View>(R.id.container_info_map)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            addMarkers(googleMap)
            googleMap.setInfoWindowAdapter(MarkerInfoAdapter(this))

            googleMap.setOnMapLoadedCallback {
                // Verifica se a permissão de localização está concedida
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Obtém a última localização conhecida
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            // Verifica se a localização é diferente de nulo e move a câmera
                            location?.let {
                                val userLatLng = LatLng(location.latitude, location.longitude)
                                googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        userLatLng,
                                        15f
                                    )
                                )

                                // Adiciona um marcador para a localização do usuário
                                val markerOptions =
                                    MarkerOptions().position(userLatLng).title("Sua Localização")
                                googleMap.addMarker(markerOptions)
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


            googleMap.setOnMapClickListener { _ ->
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

            googleMap.setOnMarkerClickListener { clickedMarker ->
                val clickedPlace = clickedMarker.tag as? Place
                if (clickedPlace != null) {
                    findViewById<TextView>(R.id.tv_name)?.text = clickedPlace.name
                    findViewById<TextView>(R.id.tv_address)?.text = clickedPlace.address
                    findViewById<TextView>(R.id.tv_reference)?.text = clickedPlace.reference

                    val newHeight = 800
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
                false
            }

        }
    }

    private fun addMarkers(googleMap: GoogleMap) {
        places.forEach { place ->
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .title(place.name)
                    .snippet(place.address)
                    .position(place.latLng)
                    .icon( /*para mudar o icon*/
                        BitmapHelper.vectorToBitmap(
                            this, R.drawable.icon_pin_location,
                            ContextCompat.getColor(this, R.color.orange)
                        )
                    )
            )
            marker?.tag = place
        }
    }

}

data class Place(
    val name: String,
    val latLng: LatLng,
    val address: String,
    val reference: String
)