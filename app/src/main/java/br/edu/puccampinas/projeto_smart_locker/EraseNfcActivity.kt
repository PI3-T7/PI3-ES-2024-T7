package br.edu.puccampinas.projeto_smart_locker

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityReadNfcBinding

class EraseNfcActivity : AppCompatActivity() {
    private val binding by lazy { ActivityReadNfcBinding.inflate( layoutInflater ) }
    private val nfcAdapter by lazy { NfcAdapter.getDefaultAdapter(this) }
    private val pendingIntent by lazy { PendingIntent.getActivity(
        this,
        0,
        Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_MUTABLE
    ) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Obter o ID da locação passado através da intent
        val idLocacao = intent.getStringExtra("idLocacao")

        binding.imgArrow.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        // Verifica se o NFC está habilitado
        if (!nfcAdapter.isEnabled) {
            showAlertMessage("A tecnologia NFC está desabilitada! Por favor, vá até as configurações do seu dispositivo e habilite o NFC.")
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

        val intentFilterTag = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, arrayOf(intentFilterNdef, intentFilterTag), null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i("teste", "Passou no onNewIntent")
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action || NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                EraseNfcTag(it)
            }
        }
    }

    private fun EraseNfcTag(tag:Tag){
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val emptyMessage = NdefMessage(arrayOf(NdefRecord.createTextRecord("", "")))
                ndef.writeNdefMessage(emptyMessage)
                ndef.close()
                Toast.makeText(this, "NFC tag apagada!", Toast.LENGTH_SHORT).show()
                // passa para a proxima activity
                val idLocacao = intent.getStringExtra("idLocacao")
                val intent = Intent(this, EndLeaseActivity::class.java)
                intent.putExtra("idLocacao", idLocacao)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                showErrorMessage("Erro ao limpar os dados cadastrados!")
            }
        }

    }

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
        textViewMessage.text = message

        // Mostre o diálogo
        alertDialog.show()
    }
}
