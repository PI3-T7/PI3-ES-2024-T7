package br.edu.puccampinas.projeto_smart_locker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity() {

    private val places = arrayListOf(
        Place(
            "PUCCAMP",
            LatLng(-22.8345605, -47.0635005),
            "Ac. Publico, 286 - Parque dos Jacarandás, Campinas - SP, 13086-061",
            4.8f
        ),
        Place(
            "Shopping Dom Pedro",
            LatLng(-22.8475613, -47.0677126),
            "Av. Guilherme Campos, 500 - Jardim Santa Genebra, Campinas - SP, 13080-000",
            4.7f
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            addMarkers(googleMap)

            googleMap.setOnMapLoadedCallback {
                val bounds = LatLngBounds.builder()
                places.forEach() {
                    bounds.include(it.latLng)
                }

                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 200))
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
        }
    }

}

data class Place(
    val name: String,
    val latLng: LatLng,
    val address: String,
    val rating: Float
)