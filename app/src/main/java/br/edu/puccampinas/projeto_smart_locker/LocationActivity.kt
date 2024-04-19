package br.edu.puccampinas.projeto_smart_locker

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LocationActivity : AppCompatActivity() {

    // Variável que acessa os serviços de localização da API
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        // Código de solicitação de permissão
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    private lateinit var btn30min : RadioButton
    private lateinit var btn1hour : RadioButton
    private lateinit var btn2hours : RadioButton
    private lateinit var btn4hours : RadioButton
    private lateinit var btnUntil18: RadioButton
    private lateinit var btnConfirmLocation: Button

    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verificar e solicitar permissão de acesso à localização
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            // Permissão já concedida, obter a localização atual
            obterLocalizacaoAtual()
        }

        btn30min = findViewById(R.id.btn30min)
        btn1hour = findViewById(R.id.btn1hour)
        btn2hours = findViewById(R.id.btn2hours)
        btn4hours = findViewById(R.id.btn4hours)
        btnUntil18 = findViewById(R.id.btnUntil18)
        btnConfirmLocation = findViewById(R.id.bt_confirm_location)

        // Obter a hora atual
        val calendario = Calendar.getInstance()
        val horaAtual = calendario.get(Calendar.HOUR_OF_DAY)

        // Verificar se está entre 7 e 8 horas
        if (horaAtual in 7..8) {
            btnUntil18.visibility = View.VISIBLE
        } else {
            btnUntil18.visibility = View.GONE
        }

        btnConfirmLocation.setOnClickListener {
            if (btn30min.isChecked) {
                // Código para 30 minutos selecionado - cadastrar locação no banco
                Toast.makeText(this, "30 minutos selecionado", Toast.LENGTH_SHORT).show()
            } else if (btn1hour.isChecked) {
                // Código para 1 hora selecionada
                Toast.makeText(this, "1 hora selecionada", Toast.LENGTH_SHORT).show()
            } else if (btn2hours.isChecked) {
                // Código para 2 horas selecionadas
                Toast.makeText(this, "2 horas selecionadas", Toast.LENGTH_SHORT).show()
            } else if (btn4hours.isChecked) {
                // Código para 4 horas selecionadas
                Toast.makeText(this, "4 horas selecionadas", Toast.LENGTH_SHORT).show()
            } else if (btnUntil18.isChecked) {
                // Código para até 18 horas selecionadas
                Toast.makeText(this, "Até 18 horas selecionadas", Toast.LENGTH_SHORT).show()
            } else {
                // Nenhum botão de rádio selecionado
                Toast.makeText(this, "Selecione uma opção de tempo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, obter a localização atual
                obterLocalizacaoAtual()
            } else {
                // Permissão negada, mostrar mensagem ou tratar de outra forma
                Toast.makeText(
                    this,
                    "Permissão de localização negada.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun obterLocalizacaoAtual() {
        // Obter a última localização conhecida do usuário
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Utilize a localização aqui
                    // Exemplo: val latitude = location.latitude
                    //           val longitude = location.longitude
                } else {
                    // Localização não disponível
                    Toast.makeText(
                        this,
                        "Não foi possível obter a localização atual.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()

        val editLocal = findViewById<TextView>(R.id.local)

        val documentReference = db.collection("Unidades de Locação").document("8W9yNMvak39ZK96EtiFu")

        Log.d(TAG, "DocumentReference: $documentReference")

        documentReference.get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")

                if (document.exists()) {
                    // O documento com o ID "1" existe, agora você pode acessar seus dados
                    val nome = document.getString("name")
                    editLocal.text = nome

                    // Acessando o array de preços
                    val pricesArray = document.get("prices") as? List<*>
                    if (pricesArray != null) {
                        // Limpar as opções anteriores
                        clearRadioButtonOptions()

                        // Adicionar os preços aos RadioButtons
                        addPricesToRadioButtons(pricesArray)
                    } else {
                        // O campo 'prices' não é um array ou está vazio
                    }
                } else {
                    // O documento com o ID "1" não existe
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting document", exception)
                // Lidar com falhas na recuperação aqui
            }

    }

    private fun clearRadioButtonOptions() {
        // Limpar os textos dos RadioButtons
        btn30min.text = ""
        btn1hour.text = ""
        btn2hours.text = ""
        btn4hours.text = ""
        btnUntil18.text = ""
    }

    private fun addPricesToRadioButtons(prices: List<*>?) {
        // Certifique-se de que há preços suficientes para cada RadioButton
        if (prices != null) {
            if (prices.size >= 5) {
                // Definir os preços nos RadioButtons
                btn30min.text = "30 minutos                                    ${prices?.get(0)},00"
                btn1hour.text = "1 hora                                          ${prices?.get(1)},00"
                btn2hours.text = "2 horas                                        ${prices?.get(2)},00"
                btn4hours.text = "4 horas                                        ${prices?.get(3)},00"
                btnUntil18.text = "Do momento até 18h                       ${prices?.get(4)},00"
            } else {
                // Não há preços suficientes
                Toast.makeText(this, "Não há preços suficientes", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
