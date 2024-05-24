package br.edu.puccampinas.projeto_smart_locker

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityReadNfcBinding

/**
 * Activity responsável pela leitura de tags NFC.
 * @author Marcos Miotto
 */
class ReadNfcActivity : AppCompatActivity() {
    private val binding by lazy { ActivityReadNfcBinding.inflate( layoutInflater ) }
    private val nfcAdapter by lazy { NfcAdapter.getDefaultAdapter(this) }
    private val pendingIntent by lazy { PendingIntent.getActivity(
        this,
        0,
        Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_MUTABLE
    ) }

    /**
     * Chamado quando a atividade é criada pela primeira vez.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.imgArrow.setOnClickListener { finish() }
        binding.btnTeste.setOnClickListener { startActivity(Intent(this,CheckDataNfcActivity::class.java)) }
    }

    /**
     * Chamado quando a atividade está prestes a se tornar visível.
     */
    override fun onResume() {
        super.onResume()

        // Verifica se o NFC está habilitado
        if (!nfcAdapter.isEnabled) {
            showAlertMessage("Aviso: A tecnologia NFC está desabilitada! Por favor, vá até as configurações do seu dispositivo e habilite o NFC.")
        }

        // Aplica o filtro NDEF
        val intentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            try {
                addDataType("text/plain")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Failed to add MIME type.", e)
            }
        }
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, arrayOf(intentFilter), null)
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
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        tag?.let {
            readNfcTag(it)
        }
    }

    /**
     * Lê uma tag NFC e processa os dados.
     * @param tag A tag NFC a ser lida.
     */
    private fun readNfcTag(tag: Tag) {
        val ndef = Ndef.get(tag)
        ndef?.let { ndefTag ->
            val ndefMessage = ndefTag.cachedNdefMessage
            ndefMessage?.let { message ->
                val records = message.records
                if (records.isNotEmpty()) {
                    val ndefRecord = records[0]
                    val payload = ndefRecord.payload
                    val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
                    val languageCodeLength = payload[0].toInt() and 51
                    val text = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, charset(textEncoding))

                    // Iniciar DisplayDataActivity com os dados da tag
                    startActivity(Intent(this, CheckDataNfcActivity::class.java).apply {
                        putExtra("tag_data", text)
                    })
                }
            }
        }
    }

    /**
     * Mostra um alerta informando que o NFC está desabilitado.
     * @param message A mensagem a ser exibida no alerta.
     */
    private fun showAlertMessage(message: String) {
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
        textViewMessage.text = message

        // Mostre o diálogo
        alertDialog.show()
    }
}
