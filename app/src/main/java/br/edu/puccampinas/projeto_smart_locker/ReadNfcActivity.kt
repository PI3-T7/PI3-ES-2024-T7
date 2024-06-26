package br.edu.puccampinas.projeto_smart_locker

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityReadNfcBinding
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity responsável pela leitura de tags NFC.
 * @author Marcos Miotto
 */
class ReadNfcActivity : AppCompatActivity() {
    private val binding by lazy { ActivityReadNfcBinding.inflate( layoutInflater ) }
    private val nfcAdapter by lazy { NfcAdapter.getDefaultAdapter(this) }
    private val database by lazy { FirebaseFirestore.getInstance() }
    private val broadcastFunction by lazy { LocalBroadcastManager.getInstance(this) }
    private val pendingIntent by lazy { PendingIntent.getActivity(
        this,
        0,
        Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_MUTABLE
    ) }
    // Definição do BroadcastReceiver para fechar a activity a partir de outra
    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "finish_read_nfc") { finish() }
        }
    }

    /**
     * Chamado quando a atividade é criada pela primeira vez.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        broadcastFunction.registerReceiver(closeReceiver, IntentFilter("finish_read_nfc"))

        binding.imgArrow.setOnClickListener { finish() }
    }

    /**
     * Chamado quando a atividade está prestes a se tornar visível.
     */
    override fun onResume() {
        super.onResume()

        // Verifica se o NFC está habilitado
        if (!nfcAdapter.isEnabled) {
            showAlertMessage()
        }

        // Aplica o filtro NDEF
        val intentFilterNdef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            try {
                addDataType("text/plain")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Failed to add MIME type.", e)
            }
        }
        // Aplica o filtro Tag Discovered
        val intentFilterTag = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, arrayOf(intentFilterNdef, intentFilterTag), null)
    }

    /**
     * Chamado quando a atividade não está mais visível para o usuário.
     */
    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    /**
     * Chamado quando a atividade recebe um novo Intent.
     * @param intent O novo Intent que foi recebido.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action || NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
//            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            @Suppress("DEPRECATION") val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                //método novo para os SDK mais novos
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else{
                //método deprecated  para os SDK mais antigos
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            tag?.let {
                readNfcTag(it)
            }
        }
    }

    /**
     * Lê uma tag NFC e processa os dados.
     * @param tag A tag NFC a ser lida.
     */
    private fun readNfcTag(tag: Tag) {
        val ndef = Ndef.get(tag)
        try {
            ndef?.let { ndefTag ->
                val ndefMessage = ndefTag.cachedNdefMessage
                ndefMessage?.let { message ->
                    val records = message.records
                    if (records.isNotEmpty()) {
                        val ndefRecord = records[0]
                        val payload = ndefRecord.payload
                        val textEncoding =
                            if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
                        val languageCodeLength = payload[0].toInt() and 51
                        val text = String(
                            payload,
                            languageCodeLength + 1,
                            payload.size - languageCodeLength - 1,
                            charset(textEncoding)
                        )
                        // Verificação se a tag possui conteudo cadastrado no banco
                        validData(text)
                    }
                }
            }
        }catch (e: Exception) {
            showErrorMessage("Aviso: Não é possível resgatar conteúdo da Tag!")
        }
    }

    // Função que valida se o dado que está na tag está dentro do firestore
    private fun validData(text: String) {
        database.collection("Locações").addSnapshotListener { value, error ->
            if (error != null){
                Log.e("Firebase Firestore Error", error.message.toString())
                return@addSnapshotListener
            }
            if (value != null){
                for (i in value.documents){
                    if(i.id == text){
                        // Iniciar DisplayDataActivity com os dados da tag
                        startActivity(Intent(this, CheckDataNfcActivity::class.java).apply {
                            putExtra("tag_data", text)
                        })
                        return@addSnapshotListener
                    }
                }
                showErrorMessage("Aviso: A Tag detectada não possui cadastro de locação!")
            }
        }
    }

    /**
     * Mostra um alerta informando que o NFC está desabilitado.
     */
    private fun showAlertMessage() {
        // Inflate o layout personalizado
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog_warning, null)

        // Crie o AlertDialog com o layout personalizado
        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        // Configure o botão OK para fechar o diálogo
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        btnOk.setOnClickListener {
            alertDialog.dismiss()
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }

        // Atualize a mensagem no TextView
        val textViewMessage = view.findViewById<TextView>(R.id.tvMessage)
        textViewMessage.text = buildString {
            append("Aviso: A tecnologia NFC está desabilitada! Por favor, vá até as configurações do seu dispositivo e habilite o NFC.")
        }

        // Mostre o diálogo
        alertDialog.show()
    }
    private fun showErrorMessage(message: String) {
        // Inflate o layout personalizado
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog_error, null)

        // Crie o AlertDialog com o layout personalizado
        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        // Configure o botão OK para fechar o diálogo
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }

        // Atualize a mensagem no TextView
        val textViewMessage = view.findViewById<TextView>(R.id.tvMessage)
        textViewMessage.text = buildString {
            append(message)
        }

        // Mostre o diálogo
        alertDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastFunction.unregisterReceiver(closeReceiver)
    }
}
