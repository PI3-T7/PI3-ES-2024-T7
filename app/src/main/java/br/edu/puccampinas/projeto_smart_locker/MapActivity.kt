package br.edu.puccampinas.projeto_smart_locker

import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val viewInfo = findViewById<View>(R.id.container_info_map)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            addMarkers(googleMap)
            googleMap.setInfoWindowAdapter(MarkerInfoAdapter(this))

            googleMap.setOnMapLoadedCallback {
                val bounds = LatLngBounds.builder()
                places.forEach() {
                    bounds.include(it.latLng)
                }

                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
            }

            googleMap.setOnMapClickListener { _ ->
                // Reduzir a altura da viewInfo para 0
                val newHeight = 1 // Altura desejada em pixels
                val anim = ValueAnimator.ofInt(viewInfo.height, newHeight)
                anim.addUpdateListener { valueAnimator ->
                    val newVal = valueAnimator.animatedValue as Int
                    val layoutParams = viewInfo.layoutParams
                    layoutParams.height = newVal
                    viewInfo.layoutParams = layoutParams
                }
                anim.duration = 300 // Duração da animação em milissegundos
                anim.start()

                // Limpar os textos
                findViewById<TextView>(R.id.tv_name)?.text = ""
                findViewById<TextView>(R.id.tv_address)?.text = ""
                findViewById<TextView>(R.id.tv_reference)?.text = ""
            }

            googleMap.setOnMarkerClickListener { clickedMarker ->
                val clickedPlace = clickedMarker.tag as? Place
                if (clickedPlace != null) {
                    // Exibir as informações do marcador no TextView
                    findViewById<TextView>(R.id.tv_name)?.text = clickedPlace.name
                    findViewById<TextView>(R.id.tv_address)?.text = clickedPlace.address
                    findViewById<TextView>(R.id.tv_reference)?.text = clickedPlace.reference

                    // Expandir a altura da viewInfo
                    val newHeight = 800 // Altura desejada em pixels
                    val anim = ValueAnimator.ofInt(viewInfo.height, newHeight)
                    anim.addUpdateListener { valueAnimator ->
                        val newVal = valueAnimator.animatedValue as Int
                        val layoutParams = viewInfo.layoutParams
                        layoutParams.height = newVal
                        viewInfo.layoutParams = layoutParams
                    }
                    anim.duration = 300 // Duração da animação em milissegundos
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