package br.edu.puccampinas.projeto_smart_locker

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityReadNfcBinding

class ReadNfcActivity : AppCompatActivity() {
    private val binding by lazy { ActivityReadNfcBinding.inflate( layoutInflater ) }
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.imgArrow.setOnClickListener { finish() }
        textView = binding.appCompatTextView5

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE)

        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            try {
                addDataType("text/plain")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Failed to add MIME type.", e)
            }
        }
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, arrayOf(intentFilter), null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                val ndef = Ndef.get(it)
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
        }
    }
}

