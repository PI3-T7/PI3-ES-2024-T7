package br.edu.puccampinas.projeto_smart_locker

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Classe responsável por verificar o status da conexão de rede do dispositivo.
 * @param connectivityManager O ConnectivityManager fornecido pelo contexto da aplicação,
 *                            necessário para acessar informações sobre a conexão de rede.
 * @author isabellatressino
 */
class NetworkChecker(private val connectivityManager: ConnectivityManager) {

    /**
     * Executa uma ação se houver conexão de rede.
     * @param action A ação a ser executada se houver conexão de rede.
     */
    fun performActionIfConnected(action: () -> Unit) {
        if (hasInternet()) {
            action()
        }
    }

    /**
     * Verifica se há conexão de internet disponível.
     * @return true se houver conexão de internet, false caso contrário.
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

