package br.edu.puccampinas.projeto_smart_locker

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkChecker(private val connectivityManager: ConnectivityManager) {

    fun performActionIfConnected(action: () -> Unit) {
        if (hasInternet()) {
            action()
        }
    }

    /**
     * Função que retorna um Boolean se tem ou não internet
     */
    fun hasInternet(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
    }
}
