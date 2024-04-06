package br.edu.puccampinas.projeto_smart_locker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoWindow(p0: Marker): View? = null

    override fun getInfoContents(marker: Marker): View? = null
}