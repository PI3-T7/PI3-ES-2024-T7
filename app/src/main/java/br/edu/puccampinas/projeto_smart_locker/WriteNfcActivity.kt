package br.edu.puccampinas.projeto_smart_locker

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.provider.Settings
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityReadNfcBinding

class WriteNfcActivity : AppCompatActivity() {
    // Declaração do ViewBinding
    private val binding by lazy { ActivityReadNfcBinding.inflate( layoutInflater ) }
    // Declaração do adaptador NFC do celular
    private val nfcAdapter by lazy { NfcAdapter.getDefaultAdapter(this) }
    // Activity que vai ser executada quando o NFC for lido
    // ele tá configurado para quando a tag for reconhecida ele executa essa mesma activity, mas
    // quando essa mesma activity for executada ele vai realizar a função onNewIntent
    private val pendingIntent by lazy { PendingIntent.getActivity(
        this,
        0,
        Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_MUTABLE
    ) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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

        // Aplica o filtro Tag Discovered
        val intentFilterTag = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        // Configura os dois filtros
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, arrayOf(intentFilterNdef, intentFilterTag), null)
    }

    override fun onPause() {
        super.onPause()
        // Caso o usuário saia da tela ele desabilita
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Se ele reconhecer alguma tag que possui algum dos dois filtros (tag discovered ou NDEF)
        // ele pega o valor da tag e passa para uma função, no caso dessa activity é o writeNfcTag
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action || NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                writeNfcTag(it)
            }
        }
    }

    private fun writeNfcTag(tag: Tag): Boolean {
        Log.i("teste", "executou a função writeNfc")
        val ndefMessage = NdefMessage(arrayOf(
            NdefRecord.createTextRecord("en", intent.getStringExtra("location_data"))
        ))

        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (ndef.isWritable) {
                    ndef.writeNdefMessage(ndefMessage)
                    ndef.close()
                    Toast.makeText(this, "Dados escritos na tag!", Toast.LENGTH_SHORT).show()
                    // Aqui fica o start activity para a proxima activity
                    true
                } else {
                    showErrorMessage("A tag não é gravável!")
                    false
                }
            } else {
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    try {
                        ndefFormatable.connect()
                        ndefFormatable.format(ndefMessage)
                        ndefFormatable.close()
                        Toast.makeText(this, "Tag formatada e dados escritos!", Toast.LENGTH_SHORT).show()
                        // Aqui fica o start activity para a proxima activity
                        true
                    } catch (e: Exception) {
                        showErrorMessage("Falha ao formatar a tag!")
                        false
                    }
                } else {
                    showErrorMessage("Não é possível escrever nesta tag!")
                    false
                }
            }
        } catch (e: Exception) {
            showErrorMessage("Falha ao escrever a tag!")
            false
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