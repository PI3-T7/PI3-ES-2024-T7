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
import android.os.Build
import android.provider.Settings
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
    // Definição do callback do botão voltar do android
    private val callback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            showAlertCancel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configurando o botão voltar para sair da conta do app
        this.onBackPressedDispatcher.addCallback(this, callback)

        binding.textView.text = if (intent.getIntExtra("qtdeTags", 0) == 1){
            "Aproxime a pulseira Nfc do celular"
        }else if(intent.getIntExtra("qtdeTags", 0) == 2){
            "Aproxime a primeira pulseira Nfc do celular."
        }else{
            "Aproxime a segunda pulseira Nfc do celular"
        }

        // Tira a opção de voltar
        binding.imgArrow.isVisible = false
        // Coloca a opção de sair
        binding.cancelNfc.isVisible = true
        binding.cancelNfc.setOnClickListener { showAlertCancel() }
    }

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
//            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            @Suppress("DEPRECATION") val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                //método novo para os SDK mais novos
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else{
                //método deprecated  para os SDK mais antigos
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            tag?.let {
                writeNfcTag(it)
            }
        }
    }

    private fun writeNfcTag(tag: Tag): Boolean {
        val msg = intent.getStringExtra("location_data")
        val ndefMessage = NdefMessage(arrayOf(
            NdefRecord.createTextRecord("en", msg)
        ))

        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (ndef.isWritable) {
                    ndef.writeNdefMessage(ndefMessage)
                    ndef.close()
                    // Aqui fica o start activity para a proxima activity
                    if (intent.getIntExtra("qtdeTags", 0) == 1){
                        val activityIntent = Intent(this, ClosetReleasedActivity::class.java)
                            .putExtra("locacaoId", msg) // Envia o ID da locação para a próxima activity
                        startActivity(activityIntent)
                        finish()
                    }else if (intent.getIntExtra("qtdeTags", 0) == 2){
                        val activityIntent = Intent(this, WriteNfcActivity::class.java)
                            .putExtra("location_data", msg)
                            .putExtra("qtdeTags", 3)
                        startActivity(activityIntent)
                        finish()
                    } else{
                        val activityIntent = Intent(this, ClosetReleasedActivity::class.java)
                            .putExtra("locacaoId", msg) // Envia o ID da locação para a próxima activity
                        startActivity(activityIntent)
                        finish()
                    }
                    true
                } else {
                    showErrorMessage("Erro: A tag não é gravável!")
                    false
                }
            } else {
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    try {
                        ndefFormatable.connect()
                        ndefFormatable.format(ndefMessage)
                        ndefFormatable.close()
                        // Aqui fica o start activity para a proxima activity
                        if (intent.getIntExtra("qtdeTags", 0) == 2){
                            val activityIntent = Intent(this, WriteNfcActivity::class.java)
                                .putExtra("location_data", msg)
                                .putExtra("qtdeTags", 3)
                            startActivity(activityIntent)
                            finish()
                        } else{
                            val activityIntent = Intent(this, ClosetReleasedActivity::class.java)
                                .putExtra("locacaoId", msg) // Envia o ID da locação para a próxima activity
                            startActivity(activityIntent)
                            finish()
                        }
                        true
                    } catch (e: Exception) {
                        showErrorMessage("Erro: Falha ao formatar a tag!")
                        false
                    }
                } else {
                    showErrorMessage("Erro: Não é possível escrever nesta tag!")
                    false
                }
            }
        } catch (e: Exception) {
            showErrorMessage("Erro: Falha ao escrever a tag!")
            false
        }
    }

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
            append("Aviso: A tecnologia NFC está desabilitada! Por favor, vá até as configurações do seu dispositivo e habilite o NFC")
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
        textViewMessage.text = message

        // Mostre o diálogo
        alertDialog.show()
    }
    private fun showAlertCancel() {
        // Inflate o layout customizado
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_cancel_operation, null)
        dialogView.findViewById<TextView>(R.id.cancelText).text = buildString {
            append("Tem certeza que deseja sair da operação sem cadastrar a Tag?")
        }

        // Crie o AlertDialog e ajuste sua altura desejada
        val customDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Impede o fechamento do diálogo ao tocar fora dele
            .create()

        // Defina a altura desejada para o diálogo
        customDialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 600)

        // Configure os botões do diálogo
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo3)
        val btnYes = dialogView.findViewById<Button>(R.id.btnYes3)

        btnNo.setOnClickListener {
            // Fecha o diálogo sem fazer logout
            customDialog.dismiss()
        }

        btnYes.setOnClickListener {
            customDialog.dismiss()
            finish()
        }

        // Mostre o diálogo
        customDialog.show()
    }
}